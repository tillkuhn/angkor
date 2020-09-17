import {Component, OnInit} from '@angular/core';
import {ApiService} from '../shared/api.service';
import {EnvironmentService} from '../shared/environment.service';
import {NGXLogger} from 'ngx-logger';
import {MasterDataService} from '../shared/master-data.service';
import {ListItem} from '../domain/list-item';
import {Place} from '../domain/place';
import {AuthService} from '../shared/auth.service';
import {Observable} from 'rxjs';

@Component({
  selector: 'app-places',
  templateUrl: './places.component.html',
  styleUrls: ['./places.component.scss']
})
export class PlacesComponent implements OnInit {
  // icon should match https://material.io/resources/icons/

  displayedColumns: string[] = ['areaCode', 'locationType', 'name', 'coordinates'];
  items$: Observable<Place[]> ;

  constructor(private api: ApiService,
              private env: EnvironmentService,
              private logger: NGXLogger,
              private masterData: MasterDataService,
              public authService: AuthService) {
  }

  getSelectedLotype(row: Place): ListItem {
    return this.masterData.lookupLocationType(row.locationType);
  }

  ngOnInit() {
    this.items$ = this.api.getPlaces();
  }

  // https://www.google.com/maps/@51.4424832,6.9861376,13z
  // Google format is **LAT**itude followed by **LON**ngitude and Z (altitude? data grid? we don't know and don't need)
  getGoogleLink(place: Place) {
    if (place.coordinates && place.coordinates.length > 1) {
      return 'https://www.google.com/maps/search/?api=1&query=' + place.coordinates[1] + ',' + place.coordinates[0];
    } else {
      return 'no loca, chica';
    }
  }

}
