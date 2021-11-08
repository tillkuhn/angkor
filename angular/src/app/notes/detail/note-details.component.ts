import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Note} from '@app/domain/note';
import {AuthService} from '@shared/services/auth.service';
import {NGXLogger} from 'ngx-logger';
import {ListType, MasterDataService} from '@shared/services/master-data.service';
import {NoteStoreService} from '../note-store.service';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {addDays} from 'date-fns';
import {ListItem} from '@shared/domain/list-item';
import {Router} from '@angular/router';

export declare type DialogAction = 'CLOSED' | 'DELETED'; // todo move to generic

@Component({
  selector: 'app-note-details',
  templateUrl: 'note-details.component.html',
})
export class NoteDetailsComponent implements OnInit {

  private readonly className = 'NoteDetailsComponent';

  // Todo use forms like in https://blog.angular-university.io/angular-material-dialog/
  isDebug = false;
  isReadonly = false; // allow write by default ...
  authScopes: ListItem[];
  noteStates: ListItem[];
  matcher = new DefaultErrorStateMatcher();
  formData: FormGroup;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: Note,
    private logger: NGXLogger,
    private formBuilder: FormBuilder,
    public dialogRef: MatDialogRef<NoteDetailsComponent>,
    private store: NoteStoreService,
    public authService: AuthService,
    public masterData: MasterDataService,
    private router: Router, // should be handled by parent controller
  ) {
  }

  ngOnInit() {
    this.authScopes = this.masterData.getList(ListType.AUTH_SCOPE);
    this.noteStates = this.masterData.getList(ListType.NOTE_STATUS);
    this.initForm();
  }

  initForm() {
    this.formData = this.formBuilder.group({
      id: [this.data.id],
      summary: [this.data.summary, [Validators.required, Validators.minLength(3)]],
      authScope: [this.data.authScope],
      status: [this.data.status],
      primaryUrl: [this.data.primaryUrl],
      assignee: [this.data.assignee],
      dueDate: [this.data.dueDate], // default reminder date in one week
      tags: this.formBuilder.array(this.data.tags)  // to be managed tag input component
    });
    this.logger.debug(`${this.className}.initForm: Finished`);
  }


  // todo make component
  getSelectedAuthScope(): ListItem {
    return this.masterData.getListItem(ListType.AUTH_SCOPE, this.data.authScope /* this.formData.get('authScope').value */);
  }

  // todo make component
  getSelectedNoteStatus(): ListItem {
    return this.masterData.getListItem(ListType.NOTE_STATUS, this.data.status);
  }

  incrementDueDate(days: number) {
    const dueDate = this.formData.value.dueDate;
    if (dueDate) {
      this.formData.patchValue({dueDate: addDays(dueDate, days)});
    }
  }

  saveItem() {
    this.close(this.formData.value as Note);
  }

  convertToPlace() {
    this.store.convertToPlace(this.formData.value as Note)
      .subscribe(id => {
          this.logger.info(`${this.className}.convertToPlace: Success ${id}`);
          this.closeItem();
          this.router.navigate(['/places/details', id]).then(); // should be handled by parent controller
        },
        (err: any) => {
          this.logger.error(err);
        }
      );
    // this.close(this.formData.value as Note);
  }


  closeItem() {
    this.close('CLOSED');
  }

  // Read https://stackoverflow.com/questions/49172970/angular-material-table-add-remove-rows-at-runtime
  // and https://www.freakyjolly.com/angular-material-table-operations-using-dialog/#.Xxm0XvgzbmE
  deleteItem() {
    this.logger.debug(`${this.className}.deleteItem: ${this.data.id}`);
    this.store.deleteItem(this.data.id)
      .subscribe(_ => this.logger.info(`${this.className}.deleteItem:: Delete Success`), (err: any) => {
        this.logger.error(err);
      });
    // should trigger this.table.renderRows(); in parent // refresh table
    this.close('DELETED');
  }

  close(action: DialogAction | Note) {
    this.dialogRef.close(action);
  }

}
