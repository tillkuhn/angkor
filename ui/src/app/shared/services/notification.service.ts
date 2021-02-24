import {Injectable} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {NGXLogger} from 'ngx-logger';

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  readonly defaultCloseTitle = 'Got it!';

  constructor(private snackBar: MatSnackBar, protected logger: NGXLogger) {
  }

  // Config Options:
  // The length of time in milliseconds to wait before automatically dismissing the snack bar.
  // horizontalPosition: MatSnackBarHorizontalPosition ( 'start' | 'center' | 'end' | 'left' | 'right';)
  // verticalPosition: MatSnackBarVerticalPosition // 'top' | 'bottom';
  // politeness: AriaLivePoliteness
  /**
   * Transport Error info to the User ...
   */
  error(message: string) {
    this.snackBar.open(`⛔ ${message}`, this.defaultCloseTitle,
      {duration: 10000, horizontalPosition: 'center'});
  }

  /**
   * Transport warn message to the User ...
   */
  warn(message: string) {
    this.snackBar.open(`😞 ${message}`, this.defaultCloseTitle,
      {duration: 7500, horizontalPosition: 'center'});
  }

  /**
   * Transport Error info to the User ...
   */
  info(message: string) {
    this.snackBar.open(`👍 ${message}`, this.defaultCloseTitle,
      {duration: 2000});
  }

}
