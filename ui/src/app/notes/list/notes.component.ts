import {ApiService} from '../../shared/api.service';
import {AuthService} from '../../shared/auth.service';
import {COMMA, ENTER} from '@angular/cdk/keycodes';
import {Component, OnInit, ViewChild} from '@angular/core';
import {DEFAULT_AUTH_SCOPE, ListType, MasterDataService, NOTE_STATUS_CLOSED} from '../../shared/master-data.service';
import {DefaultErrorStateMatcher} from '../../shared/form-helper';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatChipInputEvent} from '@angular/material/chips';
import {MatDialog} from '@angular/material/dialog';
import {MatTable} from '@angular/material/table';
import {NGXLogger} from 'ngx-logger';
import {NoteDetailsComponent} from '../detail/note-details.component';
import {Note} from '../../domain/note';
import {ActivatedRoute} from '@angular/router';
import {Location} from '@angular/common';
import {EnvironmentService} from '../../shared/environment.service';
import {NotificationService} from '../../shared/services/notification.service';

@Component({
  selector: 'app-notes',
  templateUrl: './notes.component.html',
  styleUrls: ['./notes.component.scss', '../../shared/components/chip-list/chip-list.component.scss']
})
export class NotesComponent implements OnInit {

  displayedColumns: string[] = ['status', 'summary', /*'createdAt' 'dueDate' 'actions' */];
  matcher = new DefaultErrorStateMatcher();
  items: Note[] = [];

  @ViewChild(MatTable, {static: true}) table: MatTable<any>;

  formData: FormGroup;

  // props for tag chip support, https://stackoverflow.com/questions/52061184/input-material-chips-init-form-array
  selectable = true;
  removable = true;
  addOnBlur = true;
  readonly separatorKeysCodes: number[] = [ENTER, COMMA];

  constructor(private api: ApiService,
              public env: EnvironmentService,
              private logger: NGXLogger,
              private formBuilder: FormBuilder,
              private notifier: NotificationService,
              private dialog: MatDialog,
              private route: ActivatedRoute,
              // manipulate location w/o rerouting https://stackoverflow.com/a/39447121/4292075
              private location: Location,
              public masterData: MasterDataService,
              public authService: AuthService) {
  }

  ngOnInit() {
    this.initForm();
    this.api.getNotes('')
      // .pipe(filter(num => num % 2 === 0))
      .subscribe((apiItems: Note[]) => {
        this.items = apiItems.filter(apiItem => apiItem.status !== NOTE_STATUS_CLOSED);
        this.logger.debug(`getNotes() ${this.items.length} unclosed items`);
        // if called with /notes/:id, open details popup
        if (this.route.snapshot.params.id) {
          let foundParamId = false;
          const detailsId = this.route.snapshot.params.id;
          this.items.forEach((item, index) => {
            if (item.id === detailsId) {
              foundParamId = true;
              this.logger.debug(`Try to focus on ${detailsId} ${item.summary}`);
              this.openDetailsDialog(item, index);
            }
          });
          if (! foundParamId) {
            this.notifier.warn('️Item not found or accessible, maybe you are not authenticated?');
          }
        }
      }, err => {
        this.logger.error(err);
      });
  }

  initForm() {
    this.formData = this.formBuilder.group({
      summary: [null, Validators.required],
      authScope: [DEFAULT_AUTH_SCOPE],
      primaryUrl: [null],
      tags: this.formBuilder.array([]),
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

  getNoteStatus(key: string) {
    return this.masterData.getListItem(ListType.NOTE_STATUS, key);
  }

  addTag(e: MatChipInputEvent) {
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

  removeTag(i: number) {
    const control = this.formData.controls.tags as FormArray;
    control.removeAt(i);
  }

  onFormSubmit() {
    // this.newItemForm.patchValue({tags: ['new']});
    // yyyy-MM-dd
    this.logger.info(this.formData.value.dueDate, typeof this.formData.value.dueDate);
    this.api.addNote(this.formData.value)
      .subscribe((res: any) => {
        const id = res.id;
        this.notifier.info('Quicknote saved with id ' + id);
        this.initForm(); // reset new note form
        this.items.unshift(res); // add new item to top of datasource
        this.table.renderRows(); // refresh table
        // this.ngOnInit(); // reset / reload list
        // this.router.navigate(['/place-details', id]);
      }, (err: any) => {
        this.logger.error(err);
      });
  }

  // https://stackoverflow.com/questions/60454692/angular-mat-table-row-highlighting-with-dialog-open -->
  // Tutorial https://blog.angular-university.io/angular-material-dialog/
  openDetailsDialog(row: Note, rowid: number): void {
    // this.logger.debug(this.location.path()); // e.g. /notes
    const previousLocation = this.location.path();
    if (previousLocation.indexOf(row.id) < 0) {
      this.location.go(`${previousLocation}/${row.id}`); // append id so we can bookmark
    }
    const dialogRef = this.dialog.open(NoteDetailsComponent, {
      width: '95%',
      maxWidth: '600px',
      data: row
    }).afterClosed()
      .subscribe(data => {
        this.location.go(previousLocation); // restore
        this.logger.debug(`Dialog was closed result ${data} type ${typeof data}`);
        // Delete event
        if (data === 'CLOSED') {
          this.logger.debug('Dialog was closed');
        } else if (data === 'DELETED') {
          this.logger.debug(`Note with rowid ${rowid} was deleted`);
          if (rowid > -1) {
            this.items.splice(rowid, 1);
            this.table.renderRows(); // refresh table
          }
          // Update event
        } else if (data) { // data may be null if dialogue was just closed
          // https://codeburst.io/use-es2015-object-rest-operator-to-omit-properties-38a3ecffe90 :-)
          const {createdAt, ...reducedNote} = data;
          const item = reducedNote as Note;
          this.api.updateNote(item.id, item)
            .subscribe((res: any) => {
                this.notifier.info('Note has been successfully updated');
                // .navigateToItemDetails(res.id);
              }, (err: any) => {
                this.notifier.error('Note update Error: ' + err);
              }
            );
        }
      });
    // .pipe(tap(() => /* this.activatedRow = null*/ this.logger.debug('Details Dialogue closed')));
    // dialogRef.afterClosed().subscribe(dialogResponse => {
  }

  // todo make component
  getChipClass(tag: string) {
    let suffix = '';
    if (tag === 'dringend') {
      suffix = '-red';
    } else if (tag === 'travel' || tag === 'veggy') {
      suffix = '-green';
    } else if (tag === 'tv' || tag === 'watch') {
      suffix = '-blue';
    }
    return `app-chip${suffix}`;
  }

}
