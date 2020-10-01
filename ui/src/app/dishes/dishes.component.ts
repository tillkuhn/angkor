import {Component, OnInit} from '@angular/core';
import {ApiService} from '../shared/api.service';
import {EnvironmentService} from '../shared/environment.service';
import {NGXLogger} from 'ngx-logger';
import {Dish} from '../domain/dish';
import {MasterDataService} from '../shared/master-data.service';
import {Subject} from 'rxjs';
import {debounceTime, distinctUntilChanged, filter, switchMap, tap} from 'rxjs/operators';

@Component({
  selector: 'app-dishes',
  templateUrl: './dishes.component.html',
  styleUrls: ['./dishes.component.scss']
})
export class DishesComponent implements OnInit {

  displayedColumns: string[] = ['areaCode', 'name', 'authScope', 'primaryUrl'];
  data: Dish[] = [];
  search = '';
  keyUp$ = new Subject<string>();
  isLoading = false;

  constructor(private api: ApiService,
              private env: EnvironmentService,
              private masterData: MasterDataService,
              private logger: NGXLogger) {
  }

  ngOnInit() {
    this.searchDishes();
  }

  searchDishes() {
    this.keyUp$.pipe(
      filter(term => term.length >= 3),
      debounceTime(500),
      distinctUntilChanged(),
      tap(() => this.isLoading = true),
      switchMap(searchTerm => this.api.getDishes(searchTerm)),
      tap(() => this.isLoading = false)
    )
      .subscribe(dishes => this.data = dishes);
  }

   getDishes() {
    this.api.getDishes(this.search)
      .subscribe((res: any) => {
        this.data = res;
        this.logger.debug('getDishes()', this.data);
      }, err => {
        this.logger.error(err);
      });
  }

}
