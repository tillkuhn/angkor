import {Injectable} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {NGXLogger} from 'ngx-logger';

export interface Notifier {
  error(operation: string, message: string);
  warn(operation: string, message: string);
  success(operation: string, message: string);
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService implements Notifier {

  readonly defaultCloseTitle = 'Got it!';

  constructor(private snackBar: MatSnackBar,
              protected logger: NGXLogger) {
  }

  // Config Options:
  // The length of time in milliseconds to wait before automatically dismissing the snack bar.
  // horizontalPosition: MatSnackBarHorizontalPosition ( 'start' | 'center' | 'end' | 'left' | 'right';)
  // verticalPosition: MatSnackBarVerticalPosition // 'top' | 'bottom';
  // politeness: AriaLivePoliteness
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
