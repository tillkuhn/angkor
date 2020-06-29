import {Component, OnInit} from '@angular/core';
import {ApiService} from '../api.service';
import {Place} from '../domain/place';
import {EnvironmentService} from '../environment.service';
import {NGXLogger} from 'ngx-logger';

@Component({
  selector: 'app-places',
  templateUrl: './places.component.html',
  styleUrls: ['./places.component.scss']
})
export class PlacesComponent implements OnInit {
  // icon should match https://material.io/resources/icons/
  locationType: any = {
    PLACE: {name: 'Place', icon: 'place'},
    ACCOM: {name: 'Accomodation', icon: 'hotel'},
    BEACH: {name: 'Beach & Island', icon: 'beach_access'},
    CITY: {name: 'Citytrip', icon: 'location_city'},
    EXCURS: {name: 'Excursion & Activities', icon: 'directions_walk'},
    MONUM: {name: 'Monument', icon: 'account_balance'},
    MOUNT: {name: 'Mountain & Skiing', icon: 'ac_unit'},
    ROAD: {name: 'Roadtrip Destination', icon: 'directions:car'}
  }

  displayedColumns: string[] = ['country', 'lotype', 'name', 'summary', 'coordinates'];
  data: Place[] = [];
  isLoadingResults = true;

  constructor(private api: ApiService, private env: EnvironmentService, private logger: NGXLogger) {
  }

  ngOnInit() {
    this.api.getPlaces()
      .subscribe((res: any) => {
        this.data = res;
        this.logger.debug('getPlaces()', this.data);
        this.isLoadingResults = false;
      }, err => {
        this.logger.error(err);
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
