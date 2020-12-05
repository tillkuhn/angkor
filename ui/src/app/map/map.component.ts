import {Component, OnInit} from '@angular/core';
import {EnvironmentService} from '../shared/environment.service';
import {NGXLogger} from 'ngx-logger';
import {MapboxGeoJSONFeature, MapLayerMouseEvent} from 'mapbox-gl';
import {ApiService} from '../shared/api.service';
import {Feature, Point} from 'geojson';
import {POI} from '../domain/poi';
import {environment} from '../../environments/environment';
@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit {

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

  coordinates = [18, 18]; // default center, [100.523186, 13.736717] = bangkok

  // https://docs.mapbox.com/help/glossary/zoom-level/
  zoom = [2]; // 10 ~ detailed like bangkok + area, 5 ~ southease asia, 0 ~ the earth

  accessToken = this.envservice.mapboxAccessToken;
  points: GeoJSON.FeatureCollection<GeoJSON.Point>;
  selectedPOI: MapboxGeoJSONFeature | null;
  cursorStyle: string;

  constructor(private envservice: EnvironmentService,
              private apiService: ApiService,
              private logger: NGXLogger) {
  }

  onMapboxStyleChange(entry: { [key: string]: any }) {
    this.logger.info('Switch to mapbox://styles/mapbox/' + entry.id);
    this.mapstyle = 'mapbox://styles/mapbox/' + entry.id;
  }

  ngOnInit(): void {
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
              // Todo: Map of https://labs.mapbox.com/maki-icons/
              // available out of the box, e.g. vetenary etc.
              icon: 'attraction'
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

  getThumbnail(imgageUrl: string): string {
    if (imgageUrl === null || imgageUrl === undefined || (!imgageUrl.startsWith(environment.apiUrlImagine))) {
      return '';
    }
    const newUrl = imgageUrl.replace('?large', '?small');
    return newUrl;
  }

  onPOIClick(evt: MapLayerMouseEvent) {
    // https://wykks.github.io/ngx-mapbox-gl/demo/edit/center-on-symbol
    // this.selectedPoint = evt.features![0];
    this.selectedPOI = evt.features[0];
    // center map
    this.coordinates = (evt.features[0].geometry as Point).coordinates;
    this.zoom = [this.onClickPoiZoom]; // zoom in
  }

}
