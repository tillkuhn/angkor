import {Component} from '@angular/core';
import {AuthService} from '@shared/services/auth.service';
import {ConfirmDialogComponent, ConfirmDialogModel, ConfirmDialogResult} from '@shared/components/confirm-dialog/confirm-dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {NotificationService} from '@shared/services/notification.service';
import {switchMap} from 'rxjs/operators';
import {iif, of} from 'rxjs';
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

    // https://stackoverflow.com/questions/50452947/rxjs-conditional-switchmap-based-on-a-condition
    dialogRef.afterClosed()
      .pipe(
        switchMap(dialogResult =>
          // Rxjs conditional switchMap based on a condition: https://stackoverflow.com/a/58800098/4292075
          // iif: RxJS iif() operator is a creation operator used to decide which observable will be subscribed at subscription time.
          // return either this.authService.removeMe$()  http observable simple false boolean result turned into an observable
          iif(() => (dialogResult as ConfirmDialogResult).confirmed, this.authService.removeMe$(), of({result: false}))
        )
      )
      .subscribe({
        next: (result) => {
          // is called when http returns {"result":true}
          this.notifications.success('Removal request to be fully implemented soon!' + JSON.stringify(result));
          this.logger.info('outcome' + JSON.stringify(result));
        },
        error: (error) => this.logger.error('err' + JSON.stringify(error)), // e.g. if http fails err{"headers":{"normalizedNames":{},"lazyUpdate":null},"status":500,"statusText":"Internal Server Error"
        complete: () => this.logger.info('remove me trigger action completes')
      });
  }

}
