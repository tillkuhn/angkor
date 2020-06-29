import {Component, OnInit} from '@angular/core';
import {EnvironmentService} from '../environment.service';
import {NGXLogger} from "ngx-logger";
import {MapboxGeoJSONFeature, MapLayerMouseEvent} from "mapbox-gl";
import {ApiService} from "../api.service";
import {Feature} from 'geojson';
import {POI} from '../domain/poi';

@Component({
  selector: 'app-map',
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit {
  // zoom into ... The latitude of Bangkok, Thailand is 13.736717, and the longitude is 100.523186.
  //  check https://docs.mapbox.com/mapbox-gl-js/example/setstyle/ for alternative styles, streets-v11,
  // https://docs.mapbox.com/api/maps/#styles
  mapstyles = [
    {
      description: 'Outdoor',
      id: 'outdoors-v11'
    },
    {
      description: 'Satellite',
      id: 'satellite-streets-v11' // 'satellite-v9' is w/o streets
    },
    {
      description: 'Street',
      id: 'streets-v11'
    }
  ];
  mapstyle = 'mapbox://styles/mapbox/' + this.mapstyles[0].id; // default outdoor
  // [51.2097352,35.6970118] teheran ~middle between europe + SE asia
  // [100.523186, 13.736717] = bangkok
  coordinates = [51.2097352,35.6970118] ;
  zoom = [3]; // 10 ~ detailed like bangkok + area, 5 ~ southease asia
  accessToken = this.envservice.mapboxAccessToken
  points: GeoJSON.FeatureCollection<GeoJSON.Point>;
  selectedPoint: MapboxGeoJSONFeature | null;
  cursorStyle: string;

  constructor(private envservice: EnvironmentService,
              private apiService: ApiService,
              private logger: NGXLogger) {
  }

  ngOnInit(): void {
    this.logger.info('Mapper is ready token len=', this.envservice.mapboxAccessToken.length)
    //  console.log('token', this.envservice.mapboxAccessToken, 'version',this.envservice.version)
    this.apiService.getPOIs()
      // .query()
      // .pipe(
      //   filter((res: HttpResponse<IPoi[]>) => res.ok),
      //   map((res: HttpResponse<IPoi[]>) => res.body)
      // )
      .subscribe((res: POI[]) => {
        const features: Array<Feature<GeoJSON.Point>> = [];
        res.forEach(poi => {
          if (!poi.coordinates) {
            this.logger.warn(poi.id + ' empty coordinates');
            return;
          }
          features.push({
            type: 'Feature',
            properties: {
              // tslint:disable-next-line:max-line-length
              description:
                '<strong>' +
                poi.name +
                '</strong><p>Visit it <a href="/place-details/' +
                poi.id + '" target="_place" title="Opens">' + 'here</a> </p>',
              // https://labs.mapbox.com/maki-icons/
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


  // pois: IPoi[];

  onClick(evt: MapLayerMouseEvent) {
    // this.selectedPoint = evt.features![0];
    // 50:26  error    This assertion is unnecessary ... typescript-eslint/no-unnecessary-type-assertion ß?
    this.selectedPoint = evt.features[0];
  }

  onMapboxStyleChange(entry: { [key: string]: any }) {
    // clone the object for immutability
    // clone the object for immutability
    // eslint-disable-next-line no-console
    this.logger.info('Switch to mapbox://styles/mapbox/' + entry.id);
    this.mapstyle = 'mapbox://styles/mapbox/' + entry.id;
  }
}
