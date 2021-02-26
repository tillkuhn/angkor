import {AuthService} from '../../shared/services/auth.service';
import {Component, OnInit, ViewChild} from '@angular/core';
import {DEFAULT_AUTH_SCOPE, ListType, MasterDataService, NOTE_STATUS_CLOSED} from '../../shared/services/master-data.service';
import {DefaultErrorStateMatcher} from '../../shared/form-helper';
import {FormArray, FormBuilder, FormGroup, Validators} from '@angular/forms';
import {MatDialog} from '@angular/material/dialog';
import {MatTable} from '@angular/material/table';
import {NGXLogger} from 'ngx-logger';
import {NoteDetailsComponent} from '../detail/note-details.component';
import {Note} from '../../domain/note';
import {ActivatedRoute} from '@angular/router';
import {Location} from '@angular/common';
import {EnvironmentService} from '../../shared/services/environment.service';
import {NotificationService} from '../../shared/services/notification.service';
import {NoteStoreService} from '../note-store.service';
import {SearchRequest} from '../../domain/search-request';
import {addDays} from 'date-fns';

@Component({
  selector: 'app-notes',
  templateUrl: './notes.component.html',
  styleUrls: ['./notes.component.scss', '../../shared/components/chip-list/chip-list.component.scss']
})
export class NotesComponent implements OnInit {

  displayedColumns: string[] = ['status', 'summary', /*'createdAt' 'dueDate' 'actions' */];
  matcher = new DefaultErrorStateMatcher();
  items: Note[] = [];
  searchRequest: SearchRequest = new SearchRequest();
  @ViewChild(MatTable, {static: true}) table: MatTable<any>;

  formData: FormGroup;

  constructor( /*private api: ApiService,*/
               private logger: NGXLogger,
               public store: NoteStoreService,
               public env: EnvironmentService,
               public masterData: MasterDataService,
               public authService: AuthService,
               private notifier: NotificationService,
               private formBuilder: FormBuilder,
               private dialog: MatDialog,
               private route: ActivatedRoute,
               // manipulate location w/o rerouting https://stackoverflow.com/a/39447121/4292075
               private location: Location) {
  }

  ngOnInit() {
    this.searchRequest.primarySortProperty = 'createdAt';
    this.searchRequest.reverseSortDirection();
    this.initForm();
    this.store.searchItems(this.searchRequest)
      .subscribe((apiItems: Note[]) => {
        this.items = apiItems.filter(apiItem => apiItem.status !== NOTE_STATUS_CLOSED);

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
          if (!foundParamId) {
            this.notifier.warn('️Item not found or accessible, maybe you are not authenticated?');
          }
        }
      }, err => {
        this.logger.error(err);
      });
  }

  initForm() {
    this.formData = this.formBuilder.group({
      summary: [null, [Validators.required, Validators.minLength(3)]],
      authScope: [DEFAULT_AUTH_SCOPE],
      primaryUrl: [null],
      dueDate: [addDays(new Date(), 7)], // default reminder date in one week
      tags: this.formBuilder.array(['new'])  // to be managed tag input component
    });
  }

  resetForm() {
    this.formData.reset();
    if (this.formData.controls.tags) {
      // this.logger.info('Clear');
      // this.formData.controlsthis.formBuilder.array([])
      (this.formData.controls.tags as FormArray).clear();
    }
    this.formData.patchValue({authScope: DEFAULT_AUTH_SCOPE});
  }

  onFormSubmit() {
    // this.newItemForm.patchValue({tags: ['new']});
    this.logger.info(`Submit ${JSON.stringify(this.formData.value)}`);
    this.store.addItem(this.formData.value)
      .subscribe((res: Note) => {
        const id = res.id;
        this.resetForm(); // reset new note form
        this.items.unshift(res); // add new item to top of datasource
        this.table.renderRows(); // refresh table
        // this.router.navigate(['/place-details', id]);
      }, (err: any) => {
        this.logger.error(err);
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


  // todo make component
  getChipClass(tag: string) {
    let suffix = '';
    if (tag === 'dringend' || tag === 'new') {
      suffix = '-red';
    } else if (tag === 'travel' || tag === 'veggy') {
      suffix = '-green';
    } else if (tag === 'tv' || tag === 'watch') {
      suffix = '-blue';
    }
    return `app-chip${suffix}`;
  }

  // https://stackoverflow.com/questions/60454692/angular-mat-table-row-highlighting-with-dialog-open -->
  // Tutorial https://blog.angular-university.io/angular-material-dialog/
  openDetailsDialog(row: Note, rowid: number): void {
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
          this.logger.trace('Dialog was just closed - no submit');
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
          this.store.updateItem(item.id, item)
            .subscribe((res: Note) => {
                // this.notifier.info('Note has been successfully updated');
              this.logger.info(`API returned new note ${res.id}`);
              this.items[rowid] = res; // update in existing table
              this.table.renderRows(); // refresh table
              // .navigateToItemDetails(res.id);
              }, (err: any) => {
                this.notifier.error('Note update Error: ' + err);
              }
            );
        }
      });
  }

}
