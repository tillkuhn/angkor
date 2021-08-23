import {Injectable} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {NGXLogger} from 'ngx-logger';
import {EntityEventService} from '@shared/services/entity-event.service';
import {EntityType} from '@shared/domain/entities';
import {TransformHelper} from '@shared/pipes/transform-helper';

export interface Notifier {
  error(operation: string, message: string);
  warn(operation: string, message: string);
  success(operation: string, message: string);
}

/**
 * Central notification services to handle important
 * application events
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService implements Notifier {

  private readonly className = 'NotificationService';
  readonly defaultCloseTitle = 'Got it!';

  constructor(private snackBar: MatSnackBar,
              private events: EntityEventService,
              protected logger: NGXLogger) {
    this.logger.info(`${this.className}.init: Subscribing to Entity Events`);
    events.entityEvent$.subscribe( event => this.success(`${this.friendlyEntityType(event.entityType)} ${event.entity?.id} successfully ${event.action.toLowerCase()}d`));
    events.errorEvent$.subscribe( err => this.error(err.message) );
  }

  /**
   * Transport Error info to the User ...
   */
  error(message: string) {
    this.snackBar.open(`⛔  ${message}`, this.defaultCloseTitle,
      {duration: 10000, horizontalPosition: 'center'});
  }

  /**
   * Transport warn message to the User ...
   */
  warn(message: string) {
    this.logger.warn(message);
    this.snackBar.open(`⚠️  ${message}`, this.defaultCloseTitle,
      {duration: 7500, horizontalPosition: 'center'});
  }

  /**
   * Transport Success info to the User ...
   */
  success(message: string) {
    this.snackBar.open(`👍  ${message}`, this.defaultCloseTitle,
      {duration: 2000});
  }

  private friendlyEntityType(et: EntityType) {
    return TransformHelper.titleCase(et);
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

}
