import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Note} from '../../domain/note';
import {AuthService} from '../../shared/auth.service';
import {NGXLogger} from 'ngx-logger';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ApiService} from '../../shared/api.service';

export declare type DialogAction = 'CLOSED' | 'DELETED'; // todo move to generic

@Component({
  selector: 'app-note-details',
  /*styleUrls: ['notes-details.component.scss'],*/
  templateUrl: 'note-details.component.html',
})
export class NoteDetailsComponent {

  isDebug = false;
  // Todo use forms like in https://blog.angular-university.io/angular-material-dialog/

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: Note,
    private logger: NGXLogger,
    public dialogRef: MatDialogRef<NoteDetailsComponent>,
    public authService: AuthService,
    private api: ApiService,
    private snackBar: MatSnackBar,
  ) {}

  saveItem() {
    this.close(this.data);
  }

  closeItem() {
    this.close('CLOSED');
  }


  // Read https://stackoverflow.com/questions/49172970/angular-material-table-add-remove-rows-at-runtime
  // and https://www.freakyjolly.com/angular-material-table-operations-using-dialog/#.Xxm0XvgzbmE
  // deleteRow(row: Note, rowid: number) {}
  deleteItem() {
    this.logger.debug(`Deleting ${this.data.id}`);
    this.api.deleteNote(this.data.id)
      .subscribe((res: any) => {
        this.snackBar.open('Note successfully deleted', 'Close', {
          duration: 2000,
        });
      }, (err: any) => {
        this.logger.error(err);
      });
    // should trigger this.table.renderRows(); in parent // refresh table
    this.close('DELETED');
  }

  close(action: DialogAction | Note) {
    this.dialogRef.close(action);
  }

}
