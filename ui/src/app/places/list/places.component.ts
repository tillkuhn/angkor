import {Component, OnInit} from '@angular/core';
import {MasterDataService} from '../../shared/services/master-data.service';
import {ListItem} from '../../domain/list-item';
import {Place} from '../../domain/place';
import {AuthService} from '../../shared/services/auth.service';
import {Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, switchMap} from 'rxjs/operators';
import {PlaceStoreService} from '../place-store.service';

@Component({
  selector: 'app-places',
  templateUrl: './places.component.html',
  styleUrls: ['../../shared/components/chip-list/chip-list.component.scss', '../../shared/components/common.component.scss']
})
export class PlacesComponent implements OnInit {

  toggleShowHide = false;
  sortProperties: ListItem[] = [
    {value: 'name', label: 'Name'},
    {value: 'areaCode', label: 'Region'},
    {value: 'locationType', label: 'Type'},
    {value: 'updatedAt', label: 'Updated'},
    {value: 'authScope', label: 'Authscope'}
  ];
  displayedColumns: string[] = ['areaCode', 'name'];
  // items$: Observable<Place[]> ;
  items: Place[] = [];

  minSearchTermLength = 0;
  keyUp$ = new Subject<string>();

  constructor(public store: PlaceStoreService,
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
      switchMap(() => this.store.searchItems()), // could use searchTerm as function param param but
    ).subscribe(items => this.items = items);
    // this.items$ = this.api.getDishes();
    this.runSearch();
  }

  runSearch() {
    this.store.searchItems().subscribe(items => this.items = items);
  }


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
