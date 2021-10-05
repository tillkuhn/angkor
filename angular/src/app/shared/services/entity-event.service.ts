import {Injectable} from '@angular/core';
import {EntityType, ManagedEntity} from '@shared/domain/entities';
import {Observable, Subject} from 'rxjs';
import {NGXLogger} from 'ngx-logger';
import {filter} from 'rxjs/operators';


export declare type EntityEventAction = 'CREATE' | 'UPDATE' | 'DELETE'; // todo move to generic

export interface EntityEvent {
  action: EntityEventAction;
  entityType: EntityType;
  entity?: ManagedEntity;
}

export interface ErrorEvent {
  message: string;
  operation?: string;
  error?: Error;
}

/**
 * Central Hub for entity Events
 * responsible for logging successful and error event
 * and provision them to central topics so other services can subscribe
 */
@Injectable({
  providedIn: 'root'
})
export class EntityEventService {

  private readonly className = 'EntityEventService';

  // https://stackoverflow.com/a/59103116/4292075
  // Don't use asObservable wrapper, just hide next() etc. with type casting
  private entityEventSubject: Subject<EntityEvent> = new Subject<EntityEvent>();
  public entityEvent$: Observable<EntityEvent> = this.entityEventSubject;
  private errorEventSubject: Subject<ErrorEvent> = new Subject<ErrorEvent>();
  public errorEvent$: Observable<ErrorEvent> = this.errorEventSubject;

  constructor(protected logger: NGXLogger) {
  }

  /**
   * pushes a new EntityEvent to the events subject
   * @param event
   */
  emit(event: EntityEvent) {
    this.logger.info(`${this.className}.emit: ${event.action} ${event.entityType} ${event.entity?.id}`);
    this.entityEventSubject.next(event);
  }

  /**
   * pushes a new Error Event to the error subject
   * @param event
   */
  emitError(errorEvent: ErrorEvent) {
    this.logger.error(`${this.className}.emitError: ${errorEvent.message} ${errorEvent.error ? JSON.stringify(errorEvent.error) : 'no error'}`);
    this.errorEventSubject.next(errorEvent);
  }

  /**
   * Returns an Observable that filters only events for the given eventType
   * @param entityTypeFilter
   */
  observe(entityTypeFilter: EntityType): Observable<EntityEvent> {
    return this.entityEvent$
      .pipe(
        filter(event => event.entityType === entityTypeFilter) // right not we're no interested in other entities
      );
  }

}
