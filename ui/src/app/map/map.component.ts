import { Component, OnInit } from '@angular/core';
import {EnvironmentService} from '../environment.service';

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
      description: 'Satellite',
      id: 'satellite-streets-v11' // 'satellite-v9' is w/o streets
    },
    {
      description: 'Outdoor',
      id: 'outdoors-v11'
    },
    {
      description: 'Street',
      id: 'streets-v11'
    }
  ];
  mapstyle = 'mapbox://styles/mapbox/' + this.mapstyles[0].id;
  coordinates = [100.523186, 13.736717];
  zoom = [5];
  accessToken = this.envservice.mapboxAccessToken
  // points: GeoJSON.FeatureCollection<GeoJSON.Point>;
  // selectedPoint: MapboxGeoJSONFeature | null;
  cursorStyle: string;

  constructor(private envservice: EnvironmentService ) { }

  ngOnInit(): void {
    // console.log('token', this.envservice.mapboxAccessToken, 'version',this.envservice.version)
  }



  // pois: IPoi[];


}
