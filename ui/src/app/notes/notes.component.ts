import {Component, OnInit, ViewChild} from '@angular/core';
import {Note} from '../domain/note';
import {ApiService} from '../shared/api.service';
import {NGXLogger} from 'ngx-logger';
import {FormArray, FormBuilder, FormControl, FormGroup, Validators} from '@angular/forms';
import {MatSnackBar} from '@angular/material/snack-bar';
import {DefaultErrorStateMatcher} from '../shared/form-helper';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {MatChipInputEvent} from '@angular/material/chips';
import {MatTable} from '@angular/material/table';
import {AuthService} from '../shared/auth.service';
import {ListType, MasterDataService} from '../shared/master-data.service';
import {ListItem} from '../domain/list-item';

@Component({
  selector: 'app-notes',
  templateUrl: './notes.component.html',
  styleUrls: ['./notes.component.scss', '../shared/components/chip-list/chip-list.component.scss']
})
export class NotesComponent implements OnInit {

  displayedColumns: string[] = [ 'status', 'summary', /*'createdAt' 'dueDate' */ 'actions'];
  matcher = new DefaultErrorStateMatcher();
  data: Note[] = [];
  authScopes: ListItem[];

  @ViewChild(MatTable, {static: true}) table: MatTable<any>;

  // tag chip support
  // https://stackoverflow.com/questions/52061184/input-material-chips-init-form-array
  formData: FormGroup;

  // Tag support
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];

  constructor(private api: ApiService,
              private logger: NGXLogger,
              private formBuilder: FormBuilder,
              private snackBar: MatSnackBar,
              public authService: AuthService,
              public masterData: MasterDataService) {
  }

  ngOnInit() {
    this.initForm();
    this.api.getNotes('')
      .subscribe((res: any) => {
        this.data = res;
        this.logger.debug('getNotes()', this.data);
      }, err => {
        this.logger.error(err);
      });
    this.authScopes = this.masterData.getList(ListType.AUTH_SCOPE);
  }

  initForm() {
    this.formData = this.formBuilder.group({
      summary: [null, Validators.required],
      authScope: ['ALL_AUTH'],
      tags: this.formBuilder.array([]),
      dueDate: new FormControl()
    });
  }


  // todo make component
  getSelectedAuthScope(): ListItem {
    return this.masterData.getListItem(ListType.AUTH_SCOPE, this.formData.get('authScope').value);
  }

  getNoteStatus(key: string) {
    return this.masterData.getListItem(ListType.NOTE_STATUS, key);
  }

  add(e: MatChipInputEvent) {
    const input = e.input;
    const value = e.value;
    if ((value || '').trim()) {
      const control = this.formData.controls.tags as FormArray;
      control.push(this.formBuilder.control(value.trim().toLowerCase()));
    }
    if (input) {
      input.value = '';
    }
  }

  remove(i: number) {
    const control = this.formData.controls.tags as FormArray;
    control.removeAt(i);
  }

  onFormSubmit() {
    // this.newItemForm.patchValue({tags: ['new']});
    this.api.addNote(this.formData.value)
      .subscribe((res: any) => {
        const id = res.id;
        this.snackBar.open('Quicknote saved with id ' + id, 'Close', {
          duration: 2000,
        });
        this.initForm(); // reset new note form
        this.data.push(res); // add new item to datasource
        this.table.renderRows(); // refresh table
        // this.ngOnInit(); // reset / reload list
        // this.router.navigate(['/place-details', id]);
      }, (err: any) => {
        this.logger.error(err);
      });
  }

  // Read https://stackoverflow.com/questions/49172970/angular-material-table-add-remove-rows-at-runtime
  // and https://www.freakyjolly.com/angular-material-table-operations-using-dialog/#.Xxm0XvgzbmE
  deleteRow(row: Note, rowid: number) {
    this.api.deleteNote(row.id)
      .subscribe((res: any) => {
        // const id = res.id;
        if (rowid > -1) {
          this.data.splice(rowid, 1);
          this.table.renderRows(); // refresh table
        }
        this.snackBar.open('Quicknote deleted', 'Close', {
          duration: 2000,
        });
        // this.ngOnInit(); // reset / reload list
        // this.router.navigate(['/place-details', id]);
      }, (err: any) => {
        this.logger.error(err);
      });
  }

  // todo make component
  getChipClass(tag: string) {
    let suffix = '';
    if (tag === 'dringend') {
      suffix = '-red';
    } else if (tag === 'travel' || tag === 'veggy') {
      suffix = '-green';
    }
    return `app-chip${suffix}`;
  }

}
