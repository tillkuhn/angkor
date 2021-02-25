import {Component, OnInit, ViewChild} from '@angular/core';
import {EnvironmentService} from '../shared/services/environment.service';
import {NGXLogger} from 'ngx-logger';
import {MapboxGeoJSONFeature, MapLayerMouseEvent} from 'mapbox-gl';
import {ApiService} from '../shared/services/api.service';
import {Feature, Point} from 'geojson';
import {POI} from '../domain/poi';
import {environment} from '../../environments/environment';
// we need to import as alias since we foolishly called our class also MapComponent :-)
import {MapComponent as OfficialMapComponent} from 'ngx-mapbox-gl';
import {MasterDataService} from '../shared/services/master-data.service';
import {ActivatedRoute} from '@angular/router';
import {REGEXP_COORDINATES} from '../domain/smart-coordinates';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit {

  // https://docs.mapbox.com/help/glossary/zoom-level/
  // 10 ~ detailed like bangkok + area, 5 ~ southease asia, 0 ~ the earth
  static readonly DEEPLINK_POI_ZOOM = 12; // if called with maps/@lat,lon
  static readonly ON_CLICK_POI_ZOOM = 6; // if poi is clicked
  static readonly DEFAULT_POI_ZOOM = 2; // default when /map is launached w/o args

  // https://angular-2-training-book.rangle.io/advanced-components/access_child_components
  @ViewChild(OfficialMapComponent) mapbox: OfficialMapComponent;

  // check https://docs.mapbox.com/mapbox-gl-js/example/setstyle/ for alternative styles, streets-v11,
  // https://docs.mapbox.com/api/maps/#styles
  readonly mapstyles = [
    {
      description: 'Outdoor',
      id: 'outdoors-v11'
    },
    {
      description: 'Satellite',
      id: 'satellite-streets-v11' // 'satellite-v9' is w/o streets
    } // no longer needed: {description: 'Street',id: 'streets-v11'}
  ];

  mapstyle = 'mapbox://styles/mapbox/' + this.mapstyles[0].id; // default outdoor
  cursorStyle: string;

  coordinates: number[] = [18, 18]; // default center, [100.523186, 13.736717] = bangkok lon,lat style
  zoom = [MapComponent.DEFAULT_POI_ZOOM];
  accessToken = this.envservice.mapboxAccessToken;
  points: GeoJSON.FeatureCollection<GeoJSON.Point>;
  selectedPOI: MapboxGeoJSONFeature | null;
  private locationType2Maki: Map<string, string> = new Map();

  constructor(private envservice: EnvironmentService,
              private masterData: MasterDataService,
              private apiService: ApiService,
              private route: ActivatedRoute,
              private logger: NGXLogger) {
  }

  onMapboxStyleChange(entry: { [key: string]: any }) {
    this.logger.info('Switch to mapbox://styles/mapbox/' + entry.id);
    this.mapstyle = 'mapbox://styles/mapbox/' + entry.id;
  }

  ngOnInit(): void {
    // map api locationtype enum values to Maki identifiers
    this.masterData.getLocationTypes().forEach(locationType => {
      this.locationType2Maki.set(locationType.value, locationType.maki);
    });
    this.logger.debug('Mapper is ready token len=', this.envservice.mapboxAccessToken.length);
    if (this.route.snapshot.params.coordinates) {
      const match = this.route.snapshot.params.coordinates.match(REGEXP_COORDINATES); // match[1]=lat, match[2]=lon or match==null
      if (match != null) {
        this.logger.info(`Zooming in to lat=${match[1]} lon=${match[2]}`);
        this.coordinates = [ match[2] as number, match[1] as number];
        this.zoom = [MapComponent.DEEPLINK_POI_ZOOM]; // zoom in
      } else {
        this.logger.warn(`${this.route.snapshot.params.coordinates} does not match regexp ${REGEXP_COORDINATES}`);
      }
    }
    this.apiService.getPOIs()
      .subscribe((poiList: POI[]) => {
        const features: Array<Feature<GeoJSON.Point>> = [];
        poiList.forEach(poi => {
          if (!poi.coordinates) {
            this.logger.warn(poi.id + ' empty coordinates');
            return;
          }
          // poi.coordinates[0].tof
          // does not work :-( number != number ??? https://stackoverflow.com/questions/21690070/javascript-float-comparison
          // try toFixed(4)
          // if ( (poi.coordinates[0] === this.coordinates[0])) {
          //   this.logger.info('Current coordinates match poi, try to hilight');
          // }
          features.push({
            type: 'Feature',
            properties: {
              id: poi.id,
              name: poi.name,
              areaCode: poi.areaCode,
              imageUrl: this.getThumbnail(poi.imageUrl),
              icon: this.getMakiIcon(poi.locationType)
            },
            geometry: {
              type: 'Point',
              coordinates: poi.coordinates
            }
          });
        });
        this.points = {
          type: 'FeatureCollection',
          features
        };
      });
  }

  // https://labs.mapbox.com/maki-icons/
  getMakiIcon(locationType: string) {
    return this.locationType2Maki.has(locationType) && this.locationType2Maki.get(locationType).length > 0
      ? this.locationType2Maki.get(locationType) : 'attraction';
  }

  getThumbnail(imgageUrl: string): string {
    if (imgageUrl === null || imgageUrl === undefined || (!imgageUrl.startsWith(environment.apiUrlImagine))) {
      return '';
    }
    const newUrl = imgageUrl.replace('?large', '?small');
    return newUrl;
  }

  onPOIClick(evt: MapLayerMouseEvent) {
    // https://stackoverflow.com/questions/35614957/how-can-i-read-current-zoom-level-of-mapbox
    // https://wykks.github.io/ngx-mapbox-gl/demo/edit/center-on-symbol
    // this.selectedPoint = evt.features![0];
    this.selectedPOI = evt.features[0];
    // center map at POI
    this.coordinates = (evt.features[0].geometry as Point).coordinates;
    const actualZoom = this.mapbox.mapInstance.getZoom();
    if (actualZoom < MapComponent.ON_CLICK_POI_ZOOM) {
      this.logger.debug(`Current Zoom level is ${actualZoom}, zooming in to ${MapComponent.ON_CLICK_POI_ZOOM}`);
      this.zoom = [MapComponent.ON_CLICK_POI_ZOOM]; // zoom in
    }
  }

}
