import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Link} from '../../domain/link';

@Component({
  selector: 'app-link-input',
  templateUrl: './link-input.component.html',
  styleUrls: ['./link-input.component.scss']
})
export class LinkInputComponent {

  constructor(
    public dialogRef: MatDialogRef<LinkInputComponent>,
    @Inject(MAT_DIALOG_DATA) public data: Link) {}

  onNoClick(): void {
    this.dialogRef.close();
  }

}
