import {Injectable} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {NGXLogger} from 'ngx-logger';
import {Observable, Subject} from 'rxjs';
import {EntityType} from '../../domain/entities';

export interface Notifier {
  error(operation: string, message: string);
  warn(operation: string, message: string);
  success(operation: string, message: string);
  emit(event: NotificationEvent);
}

export interface NotificationEvent {
  message: string;
  entityType: EntityType;
}

/**
 * Central notification services to handle important
 * events
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService implements Notifier {
  private readonly className = 'NotificationService';

  // https://stackoverflow.com/a/59103116/4292075
  // Don't use asObservable wrapper, just hide next() etc. with type casting
  private notificationSubject: Subject<NotificationEvent> = new Subject<NotificationEvent>();
  public notification$: Observable<NotificationEvent> = this.notificationSubject;

  readonly defaultCloseTitle = 'Got it!';

  constructor(private snackBar: MatSnackBar,
              protected logger: NGXLogger) {
  }

  /**
   * Transport Error info to the User ...
   */
  error(operation: string, message: string) {
    this.logger.error(operation, message);
    this.snackBar.open(`⛔ ${message}`, this.defaultCloseTitle,
      {duration: 10000, horizontalPosition: 'center'});
  }

  /**
   * Transport warn message to the User ...
   */
  warn(operation: string, message: string) {
    this.logger.warn(operation, message);
    this.snackBar.open(`⚠️ ${message}`, this.defaultCloseTitle,
      {duration: 7500, horizontalPosition: 'center'});
  }

  /**
   * Transport Success info to the User ...
   */
  success(operation: string, message: string) {
    this.logger.info(operation, message);
    this.snackBar.open(`👍 ${message}`, this.defaultCloseTitle,
      {duration: 2000});
  }

  emit(event: NotificationEvent) {
    this.logger.info(`${this.className}.emit: Emitting event ${JSON.stringify(event)}`);
    this.notificationSubject.next(event);
  }

}

/**
 * Can be used as default class or Unit testing
 */
export class SimpleConsoleNotifier implements Notifier {
  error(operation: string, message: string) {
    console.log('error', operation, message);
  }

  success(operation: string, message: string) {
    console.log('success', operation, message);
  }

  warn(operation: string, message: string) {
    console.log('warn', operation, message);
  }

  emit(event: NotificationEvent) {
    console.log('Noop Implementation emit Event', event);
  }

}
