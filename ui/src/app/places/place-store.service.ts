import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType} from '../domain/entities';
import {ApiService, httpOptions} from '../shared/api.service';
import {Observable} from 'rxjs';
import {Place} from '../domain/place';
import {catchError, map, tap} from 'rxjs/operators';
import {PlaceFactory} from './place-factory';
import {SearchRequest} from '../domain/search-request';
import {ListItem} from '../domain/list-item';
import {EntityStore} from '../shared/entity-store';

@Injectable({
  providedIn: 'root'
})
export class PlaceStoreService extends EntityStore<Place> {


  private readonly apiUrlPlaces = ApiService.getApiUrl(EntityType.Place);
  private readonly defaultPageSize = 100;

  sortProperties: ListItem[] = [
    {value: 'name', label: 'Name'},
    {value: 'areaCode', label: 'Region'},
    {value: 'locationType', label: 'Type'},
    {value: 'updatedAt', label: 'Updated'},
    {value: 'authScope', label: 'Authscope'}
  ];

  sortDirections: ListItem[] = [
    {value: 'ASC', label: 'Asc', icon: 'arrow_downward'},
    {value: 'DESC', label: 'Desc', icon: 'arrow_upward'}
  ];

  searchRequest: SearchRequest = {
    query: '',
    pageSize: this.defaultPageSize,
    page: 0,
    sortDirection: 'ASC',
    sortProperties: ['name']
  };

  constructor(http: HttpClient,
              logger: NGXLogger
  ) {
    super(http, logger);
  }

  entityType(): EntityType {
    return EntityType.Place;
  }

  reverseSortOrder() {
    const currentOrder = this.searchRequest.sortDirection;
    this.searchRequest.sortDirection = currentOrder === 'ASC' ? 'DESC' : 'ASC';
  }

  // todo support multiple, workaround to bind selectbox to first array element
  get sortProperty() {
    return this.searchRequest.sortProperties[0];
  }

  set sortProperty(sortprop) {
    this.searchRequest.sortProperties[0] = sortprop;
  }

  search(): Observable<Place[]> {
    if (!this.searchRequest.pageSize) {
      this.searchRequest.pageSize = this.defaultPageSize;
    }
    const operation = 'search';
    return this.http.post<Place[]>(`${this.apiUrlPlaces}/search`, this.searchRequest, httpOptions)
      .pipe(
        map(items =>
          items.map(item => PlaceFactory.fromRaw(item)),
        ),
        tap(item => item.length ?
          this.success(operation, `sortBy=${this.searchRequest.sortProperties} query="${this.searchRequest.query}" found ${item.length} places`) :
          this.success(operation, `query="${this.searchRequest.query})" found no items`)
        ),
        catchError(this.handleError(operation, []))
      );
  }

}
