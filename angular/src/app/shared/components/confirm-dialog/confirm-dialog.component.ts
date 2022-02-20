import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Component, Inject} from '@angular/core';

/**
 * Class to represent confirm dialog model.
 *
 * It is maintained here to keep it as part of shared component.
 *
 */
export class ConfirmDialogModel {

  constructor(
    public title: string,
    public message: string,
    public inputLabel?: string,
  ) {}

}

export class ConfirmDialogResult {
  input?: string;
  confirmed: boolean;

}

/**
 * General Purpose Confirmation Dialog, inspired by:
 * // https://onthecode.co.uk/create-confirm-dialog-angular-material
 *
 * Example Usage (Taken from PlaceDetailsComponent):
 *
 * confirmDeleteDialog(place: Place): void {
 *   const message = `Are you sure you want to do trash ${place.name}?`;
 *   const dialogData = new ConfirmDialogModel('Confirm Action', message);
 *   const dialogRef = this.dialog.open(ConfirmDialogComponent, {
 *     maxWidth: '400px',
 *     data: dialogData
 *   });
 *
 *   dialogRef.afterClosed().subscribe(dialogResult => {
 *     if (dialogResult) {
 *       this.deletePlace(place.id);
 *     } }); }
 *
 */
@Component({
  selector: 'app-confirm-dialog',
  templateUrl: './confirm-dialog.component.html',
  styleUrls: ['./confirm-dialog.component.scss']
})
export class ConfirmDialogComponent {

  input?: string;

  constructor(public dialogRef: MatDialogRef<ConfirmDialogComponent, ConfirmDialogResult>,
              @Inject(MAT_DIALOG_DATA) public data: ConfirmDialogModel,
  ) {
  }

  onConfirm(): void {
    // Close the dialog, return true
    this.dialogRef.close({confirmed: true, input: this.input});
  }

  onDismiss(): void {
    // Close the dialog, return false
    this.dialogRef.close({confirmed: false}); // input is irrelevant
  }
}

