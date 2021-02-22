import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType, ManagedEntity} from './domain/entities';
import {Observable, of} from 'rxjs';
import {ListItem} from './domain/list-item';
import {catchError, map, tap} from 'rxjs/operators';
import {Place} from './domain/place';
import {EntityHelper} from './entity-helper';
import {defaultPageSize, SearchRequest} from './domain/search-request';
import {MatSnackBar} from '@angular/material/snack-bar';
import {NotificationService} from './shared/services/notification.service';

export const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};

/**
 * Abstract Entity store, extend like
 * export class PlaceStoreService extends EntityStore<Place>
 */
export abstract class EntityStore<E extends ManagedEntity, AE extends ManagedEntity> {
  protected readonly className = `${this.entityType()}Store`;
  protected readonly apiUrl = EntityHelper.getApiUrl(this.entityType());

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
    const url = `${this.apiUrl}/${id}`;
    return this.http.get<AE>(url).pipe(
      map<AE, E>(apiItem => this.mapFromApiEntity(apiItem)),
      tap(_ => this.logger.debug(`${this.className}.get${this.entityType()} successfully fetched place id=${id}`)),
      catchError(this.handleError<E>(`get${this.entityType()} id=${id}`))
    );
  }

  /**
   * search items with search query (simple form, deprecated)
   * @param searchQuery
   */

  searchItems(searchRequest: SearchRequest): Observable<E[]> {
    const operation = `${this.className}.search${this.entityType()}s`;
    if (! searchRequest.pageSize) {
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
        catchError(this.handleError(operation, []))
      );
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
      tap((prod: any) => this.logger.debug(`${operation} successfully added ${this.entityType()} id=${prod.id}`)),
      tap(_ => this.notifier.info(`Well done, ${this.entityType()} has been successfully added to our DB!`)),
      catchError(this.handleError<E>(operation))
    );
  }

  /**
   * update a single item
   * @param id
   * @param item
   */
  updateItem(id: string, item: E): Observable<any> {
    const apiItem = this.mapToApiEntity(item);
    return this.http.put( `${this.apiUrl}/${id}`, apiItem, httpOptions).pipe(
      map<AE, E>(updatedApiItem => this.mapFromApiEntity(updatedApiItem)),
      tap(_ => this.logger.debug(`${this.className}.update${this.entityType()} successfully updated ${this.entityType()} id=${id}`)),
      tap(_ => this.notifier.info(`Yee-haw, ${this.entityType()} has been successfully updated!`)),
      catchError(this.handleError<any>(`update${this.entityType()}`))
    );
  }

  /**
   * Delete an existing item by id
   * @param id
   */
  deleteItem(id: string): Observable<E> {
    return this.http.delete<E>(`${this.apiUrl}/${id}`, httpOptions).pipe(
      tap(_ => this.logger.debug(`${this.className}.delete${this.entityType()} successfully deleted ${this.entityType()}  id=${id}`)),
      catchError(this.handleError<E>(`delete${this.entityType()}`))
    );
  }

  /**
   * Generic Handler for Http operations that failed.
   * Let the app continue. or nor?
   * @param operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   */
  protected handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // IMPROVEMENT: send the error to remote logging infrastructure
      this.logger.error(`${(this.className)}.${operation}  failed: ${error.message}`); // or log full $error ???
      if (error instanceof HttpErrorResponse) {
        const e = error as HttpErrorResponse;
        this.logger.warn('HttpErrorResponse message:', e.message, 'status:', e.status);
        if (e.status === 403) { // Forbidden
          //
          this.notifier.warn('Access to item is forbidden, maybe your are not authenticated?');
          // maybe in some cases also reroute: https://stackoverflow.com/a/56971256/4292075 ???
          // .onAction().subscribe(() => this.router.navigateByUrl('/app/user/detail'));
        } else if (e.status === 404) { // Not found
          this.notifier.warn('️Item not found, maybe you got the wrong Id?');
        } else if (e.status >= 500 && e.status < 599) { // Gateway Timeout
          this.notifier.error(`Unexpected server Error (${e.status}). We\'re really sorry!'`);
        }
      }
      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
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
