import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType} from '../domain/entities';
import {ApiService, httpOptions} from '../shared/api.service';
import {Observable, of} from 'rxjs';
import {Place} from '../domain/place';
import {catchError, map, tap} from 'rxjs/operators';
import {PlaceFactory} from './place-factory';
import {SearchRequest} from '../domain/search-request';
import {ListItem} from '../domain/list-item';

@Injectable({
  providedIn: 'root'
})
export class PlaceStoreService {

  private readonly apiUrlPlaces = ApiService.getApiUrl(EntityType.PLACE);
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

  constructor(private http: HttpClient,
              private logger: NGXLogger
  ) {
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
    if (! this.searchRequest.pageSize) {
      this.searchRequest.pageSize = this.defaultPageSize;
    }
    const operation = 'PlaceStoreService.search';
    return this.http.post<Place[]>(`${this.apiUrlPlaces}/search`, this.searchRequest, httpOptions)
      .pipe(
        map(items =>
          items.map(item => PlaceFactory.fromRaw(item)),
        ),
        tap(item => item.length ?
          this.logger.debug(`${operation} ${this.searchRequest.query} sortBy=${this.searchRequest.sortProperties} found ${item.length} places`) :
          this.logger.debug(`${operation} ${this.searchRequest.query}) found no items`)
        ),
        catchError(this.handleError(operation, []))
      );
  }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   * @param operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   */
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: better job of transforming error for user consumption
      this.logger.error(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

}
