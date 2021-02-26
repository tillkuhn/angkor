import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Note} from '../../domain/note';
import {AuthService} from '../../shared/services/auth.service';
import {NGXLogger} from 'ngx-logger';
import {MatSnackBar} from '@angular/material/snack-bar';
import {ListType, MasterDataService} from '../../shared/services/master-data.service';
import {ListItem} from '../../domain/list-item';
import {NoteStoreService} from '../note-store.service';

export declare type DialogAction = 'CLOSED' | 'DELETED'; // todo move to generic

@Component({
  selector: 'app-note-details',
  /*styleUrls: ['notes-details.component.scss'],*/
  templateUrl: 'note-details.component.html',
})
export class NoteDetailsComponent implements OnInit {

  // Todo use forms like in https://blog.angular-university.io/angular-material-dialog/
  isDebug = false;
  isReadonly = true;
  authScopes: ListItem[];
  noteStates: ListItem[];

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: Note,
    private logger: NGXLogger,
    public dialogRef: MatDialogRef<NoteDetailsComponent>,
    private snackBar: MatSnackBar,
    private store: NoteStoreService,
    public authService: AuthService,
    public masterData: MasterDataService
  ) {
  }

  ngOnInit() {
    this.authScopes = this.masterData.getList(ListType.AUTH_SCOPE);
    this.noteStates = this.masterData.getList(ListType.NOTE_STATUS);
  }

  // todo make component
  getSelectedAuthScope(): ListItem {
    return this.masterData.getListItem(ListType.AUTH_SCOPE, this.data.authScope /* this.formData.get('authScope').value */);
  }

  // todo make component
  getSelectedNoteStatus(): ListItem {
    return this.masterData.getListItem(ListType.NOTE_STATUS, this.data.status);
  }

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
    this.store.deleteItem(this.data.id)
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
