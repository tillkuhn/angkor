import {HttpClient, HttpHeaders} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType, ManagedEntity} from '../../domain/entities';
import {Observable} from 'rxjs';
import {catchError, map, tap} from 'rxjs/operators';
import {ApiHelper} from '../helpers/api-helper';
import {defaultPageSize, SearchRequest} from '../../domain/search-request';
import {NotificationService} from './notification.service';

export const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};

/**
 * Abstract Entity store, extend like
 * export class PlaceStoreService extends EntityStore<Place>
 */
export abstract class EntityStore<E extends ManagedEntity, AE extends ManagedEntity> {
  protected readonly className = `${this.entityType()}Store`;
  protected readonly apiUrl = ApiHelper.getApiUrl(this.entityType());

  // We want to preserve the state of the search request while the user naviates through the app
  searchRequest: SearchRequest = new SearchRequest();

  protected constructor(protected http: HttpClient,
                        protected logger: NGXLogger,
                        private notifier: NotificationService
  ) {
  }

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
      catchError(ApiHelper.handleError<any>(operation, this.notifier)) // what to return instead of any??
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
        catchError(ApiHelper.handleError(operation, this.notifier, [])) // return empty array if error
      );
  }

  clearSearch() {
    this.searchRequest.query = '';
  }

  /**
   * Create a new item
   * @param item
   */
  addItem(item: E): Observable<E> {
    const operation = `${this.className}.add${this.entityType()}`;
    const apiItem = this.mapToApiEntity(item);
    return this.http.post<AE>(this.apiUrl, apiItem, httpOptions).pipe(
      map<AE, E>(updatedApiItem => this.mapFromApiEntity(updatedApiItem)),
      tap(addedItem => this.notifier.success(operation, `Well done, ${this.entityType()} has been successfully added with id ${addedItem.id}!`)),
      tap(addedItem => this.notifier.emit({message: `${this.entityType()} ${addedItem.id} added`, entityType: this.entityType()})),
      catchError(ApiHelper.handleError<any>(operation, this.notifier)) // what to return instead of any??
    );
  }

  /**
   * update a single item
   * @param id
   * @param item
   */
  updateItem(id: string, item: E): Observable<any> {
    const operation = `${this.className}.update${this.entityType()}`;
    const apiItem = this.mapToApiEntity(item);
    return this.http.put(`${this.apiUrl}/${id}`, apiItem, httpOptions).pipe(
      map<AE, E>(updatedApiItem => this.mapFromApiEntity(updatedApiItem)),
      tap(_ => this.notifier.success(operation, `Yee-haw, ${this.entityType()} has been successfully updated!`)),
      catchError(ApiHelper.handleError<any>(operation, this.notifier))
    );
  }

  /**
   * Delete an existing item by id
   * @param id
   */
  deleteItem(id: string): Observable<E> {
    const operation = `${this.className}.delete${this.entityType()}`;
    return this.http.delete<E>(`${this.apiUrl}/${id}`, httpOptions).pipe(
      tap(_ => this.logger.debug(`${this.className}.delete${this.entityType()} successfully deleted ${this.entityType()}  id=${id}`)),
      tap(_ => this.notifier.success(operation, `Congratulations, ${this.entityType()} has been successfully removed 🗑️!`)),
      catchError(ApiHelper.handleError<any>(`delete${this.entityType()}`, this.notifier))
    );
  }

  /**
   * Default transformation from ApiEntity to UIEntity (pass through)
   * @param apiEntity
   * @protected
   */
  protected mapFromApiEntity(apiEntity: AE): E {
    this.logger.debug(`${(this.className)}.mapFromApiEntity perform default mapping`);
    return apiEntity as unknown as E;
  }

  /**
   * Default transformation to ApiEntity from UIEntity (pass through)
   * @param uiEntity
   * @protected
   */
  protected mapToApiEntity(uiEntity: E): AE {
    this.logger.debug(`${(this.className)}.mapToApiEntity perform default mapping`);
    return uiEntity as unknown as AE;
  }

}
