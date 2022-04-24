import {Component} from '@angular/core';
import {AuthService} from '@shared/services/auth.service';
import {ConfirmDialogComponent, ConfirmDialogModel, ConfirmDialogResult} from '@shared/components/confirm-dialog/confirm-dialog.component';
import {MatDialog} from '@angular/material/dialog';
import {NotificationService} from '@shared/services/notification.service';

@Component({
  selector: 'app-my-profile',
  templateUrl: './my-profile.component.html',
  styleUrls: ['../shared/components/common.component.scss']
})
export class MyProfileComponent {


  constructor(public authService: AuthService,
              private dialog: MatDialog,
              private notifications: NotificationService,
  ) {}

  confirmRequestRemoveDialog(): void {
    const message = `Are you sure you want all your data to be removed?`;
    const dialogData = new ConfirmDialogModel('Confirm Action', message);
    const dialogRef = this.dialog.open(ConfirmDialogComponent, {
      maxWidth: '400px',
      data: dialogData
    });

    dialogRef.afterClosed().subscribe(dialogResult => {
      if ((dialogResult as ConfirmDialogResult).confirmed) {
        // Removal request has been successfully sent and will be processed asap
        this.notifications.success('Removal request to be fully implemented soon!');
      }
    });
  }
}
