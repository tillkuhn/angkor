import {Component} from '@angular/core';
import {AuthService} from '@shared/services/auth.service';
import {ConfirmDialogComponent, ConfirmDialogModel, ConfirmDialogResult} from '@shared/components/confirm-dialog/confirm-dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {NotificationService} from '@shared/services/notification.service';
import {switchMap} from 'rxjs/operators';
import {iif} from 'rxjs';
import {NGXLogger} from 'ngx-logger';
import {FormGroup} from '@angular/forms';

@Component({
  selector: 'app-my-profile',
  templateUrl: './my-profile.component.html',
  styleUrls: ['../shared/components/common.component.scss']
})
export class MyProfileComponent {

  formData: FormGroup;

  constructor(public authService: AuthService,
              private dialog: MatDialog,
              private notifications: NotificationService,
              private logger: NGXLogger,
  ) {
  }

  confirmRequestRemoveDialog(): void {
    const message = `Are you sure you want all your data to be removed?`;
    const dialogData = new ConfirmDialogModel('Confirm Action', message);
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      maxWidth: '400px',
      data: dialogData
    });

    const dialogAfterCloseObservable = dialogRef.afterClosed();
    // https://stackoverflow.com/questions/50452947/rxjs-conditional-switchmap-based-on-a-condition
    dialogAfterCloseObservable
      .pipe(
        switchMap(dialogResult =>
          // Rxjs conditional switchMap based on a condition: https://stackoverflow.com/a/58800098/4292075
          // iif: Subscribe to first or second observable based on a condition
          // return either  this.authService.removeMe$() or first observable
          iif(() => (dialogResult as ConfirmDialogResult).confirmed, this.authService.removeMe$(), dialogAfterCloseObservable)
        )
      )
      // this.notifications.success('Removal request to be fully implemented soon!');
      .subscribe( {
        next: (result) => this.logger.info('outcome' + JSON.stringify(result)), // is called when http returns {"result":true}
        error: (error) => console.log('err' + JSON.stringify(error)), // e.g. if http fails err{"headers":{"normalizedNames":{},"lazyUpdate":null},"status":500,"statusText":"Internal Server Error"
        complete: (() =>  console.log('remove me trigger complete' ))
      });
  }

}
