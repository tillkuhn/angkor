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
import {MatDialog} from '@angular/material/dialog';
import {NoteDetailsComponent} from './detail/note-details.component';

@Component({
  selector: 'app-notes',
  templateUrl: './notes.component.html',
  styleUrls: ['./notes.component.scss', '../shared/components/chip-list/chip-list.component.scss']
})
export class NotesComponent implements OnInit {

  displayedColumns: string[] = ['status', 'summary', /*'createdAt' 'dueDate' */ 'actions'];
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
              private dialog: MatDialog,
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
      primaryUrl: [null],
      tags: this.formBuilder.array([]),
      dueDate: new FormControl()
    });
  }

  // parse summary for links, extract to dedicated primaryUrl Field
  parseLinks($event: any) {
    const summary = this.formData.value.summary;
    if (summary) {
      const linkRegexp = /(.*?)(https?:\/\/[^\s]+)(.*)/;
      const linkMatches = summary.match(linkRegexp);
      if (linkMatches != null) {
        const dommi = linkMatches[2].match(/(?:https?:\/\/)?(?:[^@\/\n]+@)?(?:www\.)?([^:\/?\n]+)/);
        const newSummary = linkMatches[1] + dommi[1] + linkMatches[3];
        this.formData.patchValue({summary: newSummary});
        this.formData.patchValue({primaryUrl: linkMatches[2]});
        this.logger.debug(`${summary} extracted link ${linkMatches[2]} new summary ${newSummary}`);
      }
    }
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

  /// https://stackoverflow.com/questions/60454692/angular-mat-table-row-highlighting-with-dialog-open -->
  openDetailsDialog(row: any): void {
    const dialogRef = this.dialog.open(NoteDetailsComponent, {
      width: '350px',
      data: row
    }).afterClosed()
      .subscribe(data => {
        this.logger.debug(`Dialog was closed result ${data.summary}`);
        const item = data as Note;
        this.api.updateNote(item.id, item)
          .subscribe((res: any) => {
              this.snackBar.open('Note has been successfully updated', 'Close');
              // .navigateToItemDetails(res.id);
            }, (err: any) => {
              this.snackBar.open('Note update Error: ' + err, 'Close');
            }
          );
      });
    //.pipe(tap(() => /* this.activatedRow = null*/ this.logger.debug('Details Dialogue closed')));
    // dialogRef.afterClosed().subscribe(dialogResponse => {
  }


  // Read https://stackoverflow.com/questions/49172970/angular-material-table-add-remove-rows-at-runtime
  // and https://www.freakyjolly.com/angular-material-table-operations-using-dialog/#.Xxm0XvgzbmE
  deleteRow(row: Note, rowid: number) {
    this.api.deleteNote(row.id)
      .subscribe((res: any) => {
        if (rowid > -1) {
          this.data.splice(rowid, 1);
          this.table.renderRows(); // refresh table
        }
        this.snackBar.open('Quicknote deleted', 'Close', {
          duration: 2000,
        });
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
