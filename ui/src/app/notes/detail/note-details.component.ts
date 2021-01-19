import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Note} from '../../domain/note';
import {AuthService} from '../../shared/auth.service';

@Component({
  selector: 'app-note-details',
  templateUrl: 'note-details.component.html',
})
export class NoteDetailsComponent {

  // Todo use forms like in https://blog.angular-university.io/angular-material-dialog/

  constructor(
    public dialogRef: MatDialogRef<NoteDetailsComponent>,
    public authService: AuthService,
    @Inject(MAT_DIALOG_DATA) public data: Note) {
  }


  save() {
    this.dialogRef.close(this.data);
  }

  close() {
    this.dialogRef.close();
  }

}
