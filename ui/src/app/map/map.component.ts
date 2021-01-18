import {Component, OnInit, ViewChild} from '@angular/core';
import {EnvironmentService} from '../shared/environment.service';
import {NGXLogger} from 'ngx-logger';
import {MapboxGeoJSONFeature, MapLayerMouseEvent} from 'mapbox-gl';
import {ApiService} from '../shared/api.service';
import {Feature, Point} from 'geojson';
import {POI} from '../domain/poi';
import {environment} from '../../environments/environment';
// we need to import as alias since we foolishly called our class also MapComponent :-)
import {MapComponent as OfficialMapComponent} from 'ngx-mapbox-gl';
import {ListType, MasterDataService} from '../shared/master-data.service';
import {ListItem} from '../domain/list-item';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit {

  // https://angular-2-training-book.rangle.io/advanced-components/access_child_components
  @ViewChild(OfficialMapComponent) mapbox: OfficialMapComponent;

  readonly onClickPoiZoom = 6;
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

  coordinates = [18, 18]; // default center, [100.523186, 13.736717] = bangkok

  // https://docs.mapbox.com/help/glossary/zoom-level/
  zoom = [2]; // 10 ~ detailed like bangkok + area, 5 ~ southease asia, 0 ~ the earth
  accessToken = this.envservice.mapboxAccessToken;
  points: GeoJSON.FeatureCollection<GeoJSON.Point>;
  selectedPOI: MapboxGeoJSONFeature | null;
  private locationType2Maki: Map<string, string> = new Map();

  constructor(private envservice: EnvironmentService,
              private masterData: MasterDataService,
              private apiService: ApiService,
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
    this.logger.info('Mapper is ready token len=', this.envservice.mapboxAccessToken.length);
    this.apiService.getPOIs()
      .subscribe((poiList: POI[]) => {
        const features: Array<Feature<GeoJSON.Point>> = [];
        poiList.forEach(poi => {
          if (!poi.coordinates) {
            this.logger.warn(poi.id + ' empty coordinates');
            return;
          }
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
    if (actualZoom >= this.onClickPoiZoom) {
      this.logger.debug(`No need to zoom in, current level is already at ${actualZoom}`);
    } else {
      this.logger.debug(`Current Zoom level is ${actualZoom}, zooming in to ${this.onClickPoiZoom}`);
      this.zoom = [this.onClickPoiZoom]; // zoom in
    }
  }

}
