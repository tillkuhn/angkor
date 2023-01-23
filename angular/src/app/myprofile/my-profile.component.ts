import {Component} from '@angular/core';
import {AuthService} from '@shared/services/auth.service';
import {ConfirmDialogComponent, ConfirmDialogModel, ConfirmDialogResult} from '@shared/components/confirm-dialog/confirm-dialog.component';
import {MatLegacyDialog as MatDialog} from '@angular/material/legacy-dialog';
import {NotificationService} from '@shared/services/notification.service';
import {switchMap} from 'rxjs/operators';
import {iif, of} from 'rxjs';
import {NGXLogger} from 'ngx-logger';
import {UntypedFormGroup} from '@angular/forms';

@Component({
  selector: 'app-my-profile',
  templateUrl: './my-profile.component.html',
  styleUrls: ['../shared/components/common.component.scss']
})
export class MyProfileComponent {

  formData: UntypedFormGroup;

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
        next: (response) => {
          this.logger.info('result' + JSON.stringify(response));
          if (response.result) {
            this.notifications.success('Your removal request has been submitted and will be processed asap!');
          } else {
            this.notifications.info('Operation cancelled.');
          }
        },
        error: (error) => {
          // notifications.error triggers log.error implicitly
          this.notifications.error('Could not request removal: ' + JSON.stringify(error));
        }, // e.g. if http fails err{"headers":{"normalizedNames":{},"lazyUpdate":null},"status":500,"statusText":"Internal Server Error"
        complete: () => this.logger.info('remove-me trigger action completed')
      });
  }

}
