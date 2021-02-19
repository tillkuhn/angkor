import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType, ManagedEntity} from './domain/entities';
import {Observable, of} from 'rxjs';
import {ListItem} from './domain/list-item';
import {catchError, map, tap} from 'rxjs/operators';
import {Place} from './domain/place';
import {EntityHelper} from './entity-helper';
import {SearchRequest} from './domain/search-request';

export const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};

/**
 * Abstract Entity store, extend like
 * export class PlaceStoreService extends EntityStore<Place>
 */
export abstract class EntityStore<E extends ManagedEntity, AE extends ManagedEntity> {

  readonly defaultPageSize = 100;
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

  protected readonly className = `${this.entityType()}Store`;
  protected readonly apiUrl = EntityHelper.getApiUrl(this.entityType());

  protected constructor(protected http: HttpClient,
                        protected logger: NGXLogger
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
      map(apiItem => this.mapFromApiEntity(apiItem)),
      tap(_ => this.logger.debug(`${this.className}.get${this.entityType()} successfully fetched place id=${id}`)),
      catchError(this.handleError<E>(`get${this.entityType()} id=${id}`))
    );
  }

  /**
   * search items with search query (simple form, deprecated)
   * @param searchQuery
   */

  searchItems(): Observable<E[]> {
    const operation = `${this.className}.search${this.entityType()}s`;
    if (!this.searchRequest.pageSize) {
      this.searchRequest.pageSize = this.defaultPageSize;
    }
    return this.http.post<AE[]>(`${this.apiUrl}/search`, this.searchRequest, httpOptions)
      .pipe(
        map(items =>
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
   * update a single item
   * @param id
   * @param item
   */
  updateItem(id: string, item: E): Observable<any> {
    const url = `${this.apiUrl}/${id}`;
    // const apiPlace = { ...place, authScope: (place.authScope as ListItem).value}
    return this.http.put(url, item, httpOptions).pipe(
      tap(_ => this.logger.debug(`${this.className}.update${this.entityType()} successfully updated ${this.entityType()} id=${id}`)),
      catchError(this.handleError<any>(`update${this.entityType()}`))
    );
  }

  /**
   * Create a new item
   * @param item
   */
  addItem(item: E): Observable<E> {
    const operation = `${this.className}.add${this.entityType()}`;
    return this.http.post<Place>(this.apiUrl, item, httpOptions).pipe(
      tap((prod: any) => this.logger.debug(`${operation} successfully added ${this.entityType()} id=${prod.id}`)),
      catchError(this.handleError<E>(operation))
    );
  }

  /**
   * Delete an existing item by id
   * @param id
   */
  deleteItem(id: string): Observable<E> {
    const url = `${this.apiUrl}/${id}`;
    return this.http.delete<E>(url, httpOptions).pipe(
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
        this.logger.info('message:', e.message, 'status:', e.status);
        if (e.status === 403) {
          // IMPROVEMENT inform at least the user if we know it's an error he could workaround (here: just authenticate :-))
          // this.snackBar.open('Access to item is forbidden, check if you are authenticated!', 'Acknowledge', {duration: 5000});

          // maybe in some cases also reroute: https://stackoverflow.com/a/56971256/4292075 ???
          // .onAction()
          //   .subscribe(() => this.router.navigateByUrl('/app/user/detail'));
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
