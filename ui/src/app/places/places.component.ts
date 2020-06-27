import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { Place } from '../domain/place';
import { EnvironmentService } from '../environment.service';

@Component({
  selector: 'app-places',
  templateUrl: './places.component.html',
  styleUrls: ['./places.component.scss']
})
export class PlacesComponent implements OnInit {

  displayedColumns: string[] = ['name','country', 'summary','coordinates'];
  data: Place[] = [];
  isLoadingResults = true;

  constructor(private api: ApiService, private env: EnvironmentService ) { }

  ngOnInit() {
    this.api.getPlaces()
      .subscribe((res: any) => {
        this.data = res;
        console.log('getPlaces()',this.env.version, this.data);
        this.isLoadingResults = false;
      }, err => {
        console.log(err);
        this.isLoadingResults = false;
      });
  }

  // https://www.google.com/maps/@51.4424832,6.9861376,13z
  // Google format is **LAT**itude followed by **LON**ngitude and Z (altitude? data grid? we don't know and don't need)
  getGoogleLink(place: Place) {
    if (place.coordinates.length > 1) {
      return 'https://www.google.com/maps/search/?api=1&query=' + place.coordinates[1] + ',' + place.coordinates[0];
    } else {
      return 'no loca, chica';
    }
  }

}
