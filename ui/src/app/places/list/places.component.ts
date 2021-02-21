import {Component, OnInit} from '@angular/core';
import {EnvironmentService} from '../../shared/environment.service';
import {MasterDataService} from '../../shared/master-data.service';
import {ListItem} from '../../domain/list-item';
import {Place} from '../../domain/place';
import {AuthService} from '../../shared/auth.service';
import {Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, switchMap} from 'rxjs/operators';
import {PlaceStoreService} from '../place-store.service';
import {SearchRequest} from '../../domain/search-request';

@Component({
  selector: 'app-places',
  templateUrl: './places.component.html',
  styleUrls: ['./places.component.scss', '../../shared/components/chip-list/chip-list.component.scss']
})
export class PlacesComponent implements OnInit {

  sortProperties: ListItem[] = [
    {value: 'name', label: 'Name'},
    {value: 'areaCode', label: 'Region'},
    {value: 'locationType', label: 'Type'},
    {value: 'updatedAt', label: 'Updated'},
    {value: 'authScope', label: 'Authscope'}
  ];
  searchRequest: SearchRequest = new SearchRequest();
  displayedColumns: string[] = ['areaCode', 'name'];
  // items$: Observable<Place[]> ;
  items: Place[] = [];

  minSearchTermLength = 0;
  keyUp$ = new Subject<string>();

  constructor(public store: PlaceStoreService,
              public env: EnvironmentService,
              public masterData: MasterDataService,
              public authService: AuthService) {
  }

  getLocationType(row: Place): ListItem {
    return this.masterData.lookupLocationType(row.locationType);
  }

  ngOnInit() {
    this.keyUp$.pipe(
      filter(term => term.length >= this.minSearchTermLength),
      debounceTime(500),
      distinctUntilChanged(),
      switchMap(() => this.store.searchItems(this.searchRequest) ), // could use searchTerm as function param param but
    ).subscribe(items => this.items = items);
    // this.items$ = this.api.getDishes();
    this.runSearch();
  }

  runSearch() {
    this.store.searchItems(this.searchRequest).subscribe(items => this.items = items);
  }

  clearSearch() {
    this.searchRequest.query = '';
    this.runSearch();
  }

  // https://www.google.com/maps/@51.4424832,6.9861376,13z
  // Google format is **LAT** followed by **LON** and Z (altitude? data grid? we don't know and don't need)
  // getGoogleLink(place: Place) {
  //   if (place.coordinates && place.coordinates.length > 1) {
  //     return 'https://www.google.com/maps/search/?api=1&query=' + place.coordinates[1] + ',' + place.coordinates[0];
  //   } else {
  //     return 'no location sorry';
  //   }
  // }

  // todo implement more, move to component class
  getChipClass(tag: string) {
    let suffix = '';
    if (tag === 'beach' || tag === 'sunny') {
      suffix = '-sand';
    } else if (tag === 'hot') {
      suffix = '-red';
    }
    return `app-chip${suffix}`;
  }

}
