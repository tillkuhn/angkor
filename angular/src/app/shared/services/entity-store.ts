import {ApiHelper} from '../helpers/api-helper';
import {EntityEventService} from '@shared/services/entity-event.service';
import {EntityType, ManagedEntity} from '@shared/domain/entities';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {Observable} from 'rxjs';
import {TransformHelper} from '@shared/pipes/transform-helper';
import {catchError, map, tap} from 'rxjs/operators';
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

  protected readonly className = `${TransformHelper.titleCase(this.entityType().toLowerCase())}Store`;
  protected readonly apiUrl = ApiHelper.getApiUrl(this.entityType());

  // We want to preserve the state of the search request
  // while the user navigates through the app
  searchRequest: SearchRequest = new SearchRequest();

  protected constructor(protected http: HttpClient,
                        protected logger: NGXLogger,
                        protected events: EntityEventService
  ) {
    // Subscribe to new events for our entity type (default impl: only log)
    this.events.observe(this.entityType())
      .subscribe(event => {
        logger.info(`${this.className}.entityEvents: Received event ${event.action} ${event.entityType}`);
        // this.clearCache(); // possible action
      });
  }

  // Subclasses must override this method to return their concrete entityType
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
   * Get All Items (AuthScope transparently handled by backend)
   */
  getItems(): Observable<E[]> {
    const operation = `${this.className}.getItems${this.entityType()}s`;
    return this.http.get<AE[]>(`${this.apiUrl}`, httpOptions)
      .pipe(
        map<AE[], E[]>(items =>
          items.map(item => this.mapFromApiEntity(item)),
        ),
        tap(items =>
          this.logger.debug(`${operation} found ${items.length > 0 ? items.length : 'no'} ${this.entityType()}s`)
        ),
        catchError(ApiHelper.handleError(operation, this.events, [])) // return empty array if error
      );
  }


  /**
   * Search items with search query (simple form, deprecated)
   * @param searchRequest (defaults to own member)
   */
  searchItems(searchRequest: SearchRequest = this.searchRequest): Observable<E[]> {
    const operation = `${this.className}.search()`;
    if (!searchRequest.pageSize) {
      searchRequest.pageSize = defaultPageSize;
    }
    return this.http.post<AE[]>(`${this.apiUrl}/search`, searchRequest, httpOptions)
      .pipe(
        map<AE[], E[]>(items =>
          items.map(item => this.mapFromApiEntity(item)),
        ),
        tap(items =>
          this.logger.debug(`${operation} found ${items.length > 0 ? items.length : 'no'} ${this.entityType()}s`)
        ),
        catchError(ApiHelper.handleError(operation, this.events, [])) // return empty array if error
      );
  }

  clearSearch() {
    this.searchRequest.query = '';
  }

  /**
   * Convenience function, calls updateItem() if id is present, else
   * addItem() to create a new one
   *
   * @param item
   */
  addOrUpdateItem(item: E): Observable<E> {
    return (item.id) ? this.updateItem(item.id, item) : this.addItem(item);
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
      tap(addedItem => this.events.emit({action: 'CREATE', entityType: this.entityType(), entity: addedItem})),
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
    this.logger.info(`${this.apiUrl}/${id}`);
    return this.http.put(`${this.apiUrl}/${id}`, apiItem, httpOptions).pipe(
      map<AE, E>(updatedApiItem => this.mapFromApiEntity(updatedApiItem)),
      tap(updatedItem => this.events.emit({action: 'UPDATE', entityType: this.entityType(), entity: updatedItem})),
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
      tap(_ => this.events.emit({action: 'DELETE', entityType: this.entityType(), entity: {id}})),
      catchError(ApiHelper.handleError<any>(operation, this.events))
    );
  }

  /**
   * Default transformation from ApiEntity to UIEntity (pass through)
   * @param apiEntity
   * @protected
   */
  protected mapFromApiEntity(apiEntity: AE): E {
    this.logger.trace(`${(this.className)}.mapFromApiEntity perform default 1:1 transformation`);
    return apiEntity as unknown as E;
  }

  /**
   * Default transformation to ApiEntity from UIEntity (pass through)
   * @param uiEntity
   * @protected
   */
  protected mapToApiEntity(uiEntity: E): AE {
    this.logger.trace(`${(this.className)}.mapToApiEntity perform default 1:1 transformation`);
    return uiEntity as unknown as AE;
  }

}
