import {Component, OnInit} from '@angular/core';
import {EnvironmentService} from '../../shared/environment.service';
import {NGXLogger} from 'ngx-logger';
import {Dish} from '../../domain/dish';
import {MasterDataService} from '../../shared/master-data.service';
import {Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, switchMap, tap} from 'rxjs/operators';
import {AuthService} from '../../shared/auth.service';
import {ListItem} from '../../domain/list-item';
import {SearchRequest} from '../../domain/search-request';
import {DishStoreService} from '../dish-store.service';

@Component({
  selector: 'app-dishes',
  templateUrl: './dishes.component.html',
  styleUrls: ['./dishes.component.scss', '../../shared/components/chip-list/chip-list.component.scss']
})
export class DishesComponent implements OnInit {

  toggleShowHide = false;
  sortProperties: ListItem[] = [
    {value: 'name', label: 'Name'},
    {value: 'areaCode', label: 'Region'},
    {value: 'updatedAt', label: 'Updated'},
    {value: 'authScope', label: 'Authscope'},
    {value: 'rating', label: 'Rating'}
  ];
  searchRequest: SearchRequest = new SearchRequest();

  minSearchTermLength = 0;
  displayedColumns: string[] = ['areaCode', 'name'];
  items: Dish[] = [];
  search = '';
  keyUp$ = new Subject<string>();
  isLoading = false;

  constructor(public store: DishStoreService,
              public env: EnvironmentService,
              public authService: AuthService,
              public masterData: MasterDataService
  ) {
  }

  ngOnInit() {
    this.keyUp$.pipe(
      filter(term => term.length >= this.minSearchTermLength),
      debounceTime(500),
      distinctUntilChanged(),
      tap(() => this.isLoading = true),
      switchMap(() => this.store.searchItems(this.searchRequest) ), // could use searchTerm as function param param but
      tap(() => this.isLoading = false)
  ).subscribe(items => this.items = items);
    this.runSearch();
  }

  // https://medium.com/@ole.ersoy/creating-a-conditional-clear-button-on-our-angular-material-search-field-3e2e155c6edb
  runSearch() {
    this.store.searchItems(this.searchRequest).subscribe(items => this.items = items);
  }

  clearSearch() {
    this.searchRequest.query = '';
    this.runSearch();
  }

  // todo make component
  getChipClass(tag: string) {
    let suffix = '';
    if (tag === 'spicy') {
      suffix = '-red';
    } else if (tag === 'salad' || tag === 'veggy') {
      suffix = '-green';
    }
    return `app-chip${suffix}`;
  }

}
