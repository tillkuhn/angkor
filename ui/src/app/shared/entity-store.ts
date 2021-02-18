import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType, ManagedEntity} from '../domain/entities';
import {Observable, of} from 'rxjs';

export abstract class EntityStore<E extends ManagedEntity> {

  constructor(protected http: HttpClient,
              protected logger: NGXLogger
  ) {
  }

  abstract entityType(): EntityType;

  // protected sucess(operation: string, entity: E): void { // no error
  protected success(operation: string = 'operation', msg: string): void { // no error
    const me = `${this.entityType()}Store`;
    this.logger.debug(`${me}.${operation} successfull: ${msg}`);
  }

  /**
   * Handle Http operation that failed.
   * Let the app continue.
   * @param operation - name of the operation that failed
   * @param result - optional value to return as the observable result
   */
  protected handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: better job of transforming error for user consumption
      this.logger.error(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
