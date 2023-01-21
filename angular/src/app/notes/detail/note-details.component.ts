import {Component, Inject, OnInit} from '@angular/core';
import {MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef} from '@angular/material/legacy-dialog';
import {Note} from '@app/domain/note';
import {AuthService} from '@shared/services/auth.service';
import {NGXLogger} from 'ngx-logger';
import {ListType, MasterDataService} from '@shared/services/master-data.service';
import {NoteStoreService} from '../note-store.service';
import {UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {addDays} from 'date-fns';
import {ListItem} from '@shared/domain/list-item';
import {Router} from '@angular/router';

export declare type DialogAction = 'CLOSED' | 'Deleted'; // todo move to generic

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
  formData: UntypedFormGroup;

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: Note,
    private logger: NGXLogger,
    private formBuilder: UntypedFormBuilder,
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
    return this.masterData.getListItem(ListType.AUTH_SCOPE, this.formData.get('authScope').value);
  }

  // todo make component
  getSelectedNoteStatus(): ListItem {
    return this.masterData.getListItem(ListType.NOTE_STATUS, this.formData.get('status').value);
  }

  incrementDueDate(days: number) {
    let dueDate = this.formData.value.dueDate;
    const now = new Date();
    // if due date is set and in the future, use this as a basis,
    // otherwise make sure the from-date is reset to now(), so we add days relative to the current date
    if (dueDate == null  || dueDate.getTime() <= now.getTime()) {
      this.logger.debug(`${this.className}.incrementDueDate: current dueDate was in the past, calculating based on ${now}`);
      dueDate = now;
    }
    this.formData.patchValue({dueDate: addDays(dueDate,days)});
  }

  saveItem() {
    this.close(this.formData.value as Note);
  }

  convertToPlace() {
    this.store.convertToPlace(this.formData.value as Note)
      .subscribe({
        next: id => {
          this.logger.info(`${this.className}.convertToPlace: Success ${id}`);
          this.closeItem();
          this.router.navigate(['/places/details', id]).then(); // should be handled by parent controller
        },
        error: (err: any) => {
          this.logger.error(err);
        }
      });
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
      .subscribe({
        next: _ => this.logger.info(`${this.className}.deleteItem:: Delete Success`),
        error: (err: any) => {
          this.logger.error(err);
        }
      });
    // should trigger this.table.renderRows(); in parent // refresh table
    this.close('Deleted');
  }

  close(action: DialogAction | Note) {
    this.dialogRef.close(action);
  }

}
