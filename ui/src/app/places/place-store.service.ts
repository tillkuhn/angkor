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

@Injectable({
  providedIn: 'root'
})
export class PlaceStoreService {

  private readonly apiUrlPlaces = ApiService.getApiUrl(EntityType.PLACE);

  searchRequest: SearchRequest = {
    search: '',
    size: 100,
    page: 0,
    sortDirection: 'ASC',
    sortProperties: ['name']
  };

  constructor(private http: HttpClient,
              private logger: NGXLogger
  ) {
  }

  get searchTerm() {
    return this.searchRequest.search;
  }

  set searchTerm(searchTerm) {
    this.searchRequest.search = searchTerm;
  }

  search(): Observable<Place[]> {
    const operation = 'PlaceStoreService.search';
    return this.http.post<Place[]>(`${this.apiUrlPlaces}/search`, this.searchRequest, httpOptions)
      .pipe(
        map(items =>
          items.map(item => PlaceFactory.fromRaw(item)),
        ),
        tap(item => item.length ?
          this.logger.debug(`${operation} ${this.searchRequest.search} found ${item.length} places`) :
          this.logger.debug(`${operation} ${this.searchRequest.search}) found no items`)
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
