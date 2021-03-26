import {HttpClient, HttpHeaders} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType, ManagedEntity} from '@shared/domain/entities';
import {Observable} from 'rxjs';
import {catchError, map, tap} from 'rxjs/operators';
import {ApiHelper} from '../helpers/api-helper';
import {EntityEventService} from '@shared/services/entity-event.service';
import {defaultPageSize, SearchRequest} from '@shared/domain/search-request';

export const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};

/**
 * Abstract Entity store
 *
 * Extend in you service class as follows
 * @Injectable({
 *   providedIn: 'root'
 * })
 * export class PlaceStoreService extends EntityStore<Place, ApiPlace> { (...)
 */
export abstract class EntityStore<E extends ManagedEntity, AE> {

  protected readonly className = `${this.entityType()}Store`;
  protected readonly apiUrl = ApiHelper.getApiUrl(this.entityType());

  // We want to preserve the state of the search request
  // while the user navigates through the app
  searchRequest: SearchRequest = new SearchRequest();

  protected constructor(protected http: HttpClient,
                        protected logger: NGXLogger,
                        protected events: EntityEventService
  ) {
  }

  // must override
  abstract entityType(): EntityType;

  /**
   * Get a Single item
   * @param id
   */
  getItem(id: string): Observable<E> {
    const operation = `${this.className}.get${this.entityType()}`;
    const url = `${this.apiUrl}/${id}`;
    return this.http.get<AE>(url, httpOptions).pipe(
      map<AE, E>(apiItem => this.mapFromApiEntity(apiItem)),
      tap(_ => this.logger.debug(`${operation} successfully fetched place id=${id}`)),
      catchError(ApiHelper.handleError<any>(operation, this.events)) // what to return instead of any??
    );
  }

  /**
   * search items with search query (simple form, deprecated)
   * @param searchRequest (defaults to own member)
   */
  searchItems(searchRequest: SearchRequest = this.searchRequest): Observable<E[]> {
    const operation = `${this.className}.search${this.entityType()}s`;
    if (!searchRequest.pageSize) {
      searchRequest.pageSize = defaultPageSize;
    }
    return this.http.post<AE[]>(`${this.apiUrl}/search`, searchRequest, httpOptions)
      .pipe(
        map<AE[], E[]>(items =>
          items.map(item => this.mapFromApiEntity(item)),
        ),
        tap(item => item.length ?
          this.logger.debug(`${operation} found ${item.length} ${this.entityType()}s`) :
          this.logger.debug(`${operation} found nothing`)
        ),
        catchError(ApiHelper.handleError(operation, this.events, [])) // return empty array if error
      );
  }

  clearSearch() {
    this.searchRequest.query = '';
  }

  /**
   * Create a new item
   * @param item
   * @return newly created item from API
   */
  addItem(item: E): Observable<E> {
    const operation = `${this.className}.add${this.entityType()}`;
    const apiItem = this.mapToApiEntity(item);
    return this.http.post<AE>(this.apiUrl, apiItem, httpOptions).pipe(
      map<AE, E>(updatedApiItem => this.mapFromApiEntity(updatedApiItem)),
      tap(addedItem => this.events.emit( {action: 'CREATE', entityType: this.entityType(), entity: addedItem }) ),
      catchError(ApiHelper.handleError<any>(operation, this.events)) // what to return instead of any??
    );
  }

  /**
   * Update a single item
   * @param id
   * @param item
   * @return updated item from API
   */
  updateItem(id: string, item: E): Observable<E> {
    const operation = `${this.className}.update${this.entityType()}`;
    const apiItem = this.mapToApiEntity(item);
    return this.http.put(`${this.apiUrl}/${id}`, apiItem, httpOptions).pipe(
      map<AE, E>(updatedApiItem => this.mapFromApiEntity(updatedApiItem)),
      tap(updatedItem => this.events.emit( {action: 'UPDATE', entityType: this.entityType(), entity: updatedItem }) ),
      catchError(ApiHelper.handleError<any>(operation, this.events))
    );
  }

  /**
   * Delete an existing item by id
   * @param id
   */
  deleteItem(id: string): Observable<E> {
    const operation = `${this.className}.delete${this.entityType()}`;
    return this.http.delete<E>(`${this.apiUrl}/${id}`, httpOptions).pipe(
      tap(_ => this.events.emit( {action: 'DELETE', entityType: this.entityType(), entity: {id} }) ),
      catchError(ApiHelper.handleError<any>(operation, this.events))
    );
  }

  /**
   * Default transformation from ApiEntity to UIEntity (pass through)
   * @param apiEntity
   * @protected
   */
  protected mapFromApiEntity(apiEntity: AE): E {
    this.logger.debug(`${(this.className)}.mapFromApiEntity perform default 1:1 transformation`);
    return apiEntity as unknown as E;
  }

  /**
   * Default transformation to ApiEntity from UIEntity (pass through)
   * @param uiEntity
   * @protected
   */
  protected mapToApiEntity(uiEntity: E): AE {
    this.logger.debug(`${(this.className)}.mapToApiEntity perform default 1:1 transformation`);
    return uiEntity as unknown as AE;
  }

}
