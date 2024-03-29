import {AuthService} from '@shared/services/auth.service';
import {Component, OnInit, ViewEncapsulation} from '@angular/core';
import {DEFAULT_AUTH_SCOPE, ListType, MasterDataService, NOTE_STATUS_CLOSED} from '@shared/services/master-data.service';
import {DefaultErrorStateMatcher} from '@shared/helpers/form-helper';
import {UntypedFormArray, UntypedFormBuilder, UntypedFormGroup, Validators} from '@angular/forms';
import {MatLegacyDialog as MatDialog} from '@angular/material/legacy-dialog';
import {NGXLogger} from 'ngx-logger';
import {NoteDetailsComponent} from '../detail/note-details.component';
import {Note} from '@app/domain/note';
import {ActivatedRoute} from '@angular/router';
import {Location} from '@angular/common';
import {NotificationService} from '@shared/services/notification.service';
import {NoteStoreService} from '../note-store.service';
import {addDays} from 'date-fns';
import {SearchRequest} from '@shared/domain/search-request';
import {SpeechService} from '@app/notes/speech/speech.service';

@Component({
  selector: 'app-notes',
  templateUrl: './notes.component.html',
  styleUrls: ['../../shared/components/chip-list/chip-list.component.scss', '../../shared/components/common.component.scss'],
  encapsulation: ViewEncapsulation.None // https://stackoverflow.com/a/56978906/4292075 to overwrite padding for list item
})
export class NotesComponent implements OnInit {

  displayedColumns: string[] = ['status', 'summary', /*'createdAt' 'dueDate' 'actions' */];
  matcher = new DefaultErrorStateMatcher();
  items: Note[] = [];
  searchRequest: SearchRequest = new SearchRequest();
  // @ViewChild(MatTable, {static: true}) table: MatTable<any>;

  formData: UntypedFormGroup;
  listening = false;

  constructor( /*private api: ApiService,*/
               private logger: NGXLogger,
               public store: NoteStoreService,
               public masterData: MasterDataService,
               public authService: AuthService,
               private notifier: NotificationService,
               private formBuilder: UntypedFormBuilder,
               private dialog: MatDialog,
               private route: ActivatedRoute,
               private speech: SpeechService,
               // manipulate location w/o rerouting https://stackoverflow.com/a/39447121/4292075
               private location: Location) {
  }

  ngOnInit() {
    this.searchRequest.primarySortProperty = 'createdAt';
    this.searchRequest.reverseSortDirection();
    this.initForm();
    this.store.searchItems(this.searchRequest)
      .subscribe({
        next: (apiItems: Note[]) => {
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
        },

        error: err => {
          this.logger.error(err);
        }
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
      (this.formData.controls.tags as UntypedFormArray).clear();
    }
    this.formData.patchValue({authScope: DEFAULT_AUTH_SCOPE});
  }

  onFormSubmit() {
    // this.newItemForm.patchValue({tags: ['new']});
    this.logger.info(`Submit ${JSON.stringify(this.formData.value)}`);
    this.store.addItem(this.formData.value)
      .subscribe({
        next: (res: Note) => {
          this.resetForm(); // reset new note form
          this.items.unshift(res); // add new item to top of datasource
        }, error: (err: any) => {
          this.logger.error(err);
        }
      });
  }

  // parse summary for links, extract to dedicated primaryUrl Field
  parseLinks(_: any) {
    const summary = this.formData.value.summary;
    if (summary) {
      const linkRegexp = /(.*?)(https?:\/\/[^\s]+)(.*)/;
      const linkMatches = summary.match(linkRegexp);
      if (linkMatches != null) {
        const domainMatch = linkMatches[2].match(/(?:https?:\/\/)?(?:[^@\/\n]+@)?(?:www\.)?([^:\/?\n]+)/);
        const newSummary = linkMatches[1] + domainMatch[1] + linkMatches[3];
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
    if (tag === 'urgent' || tag === 'new') {
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
  openDetailsDialog(item: Note, rowId: number): void {
    // append id to location path so we can bookmark
    const previousLocation = this.location.path();
    if (previousLocation.indexOf(item.id) < 0) {
      this.location.go(`${previousLocation}/${item.id}`);
    }

    this.dialog.open(NoteDetailsComponent, { // returns dialogRef which we could store
      width: '95%',
      maxWidth: '600px',
      data: item
    }).afterClosed()
      .subscribe(data => {
        this.location.go(previousLocation); // restore

        // Delete event
        if (data === 'CLOSED') {
          this.logger.trace('Dialog was just closed - no submit');
        } else if (data === 'CONVERTED') {
          this.logger.trace('Dialog was just closed as a result of a conversion');
          this.notifier.success('Note has been successfully converted and is now closed');
          if (rowId > -1) {
            this.items.splice(rowId, 1);
          }
        } else if (data === 'DELETED') {
          this.logger.debug(`Note with row id ${rowId} was deleted`);
          if (rowId > -1) {
            this.items.splice(rowId, 1);
          }
          // Update event
        } else if (data) { // data may be null if dialogue was just closed
          // https://codeburst.io/use-es2015-object-rest-operator-to-omit-properties-38a3ecffe90 :-)
          const {createdAt, ...reducedNote} = data;
          const note = reducedNote as Note;
          this.store.updateItem(note.id, note)
            .subscribe({
              next: (res: Note) => {
                // this.notifier.info('Note has been successfully updated');
                this.logger.info(`API returned new note ${res.id}`);
                this.items[rowId] = res; // update in existing table
                // this.table.renderRows(); // refresh table
                // .navigateToItemDetails(res.id);
              }, error: (err: any) => {
                this.notifier.error('Note update Error: ' + err);
              }
            });
        }
      });
  }

  /**
   * New support for speech recognition
   */
  listen(): void {
    this.listening = true;
    this.speech.listen().subscribe({
      next: (words) => {
        this.logger.info('Received recording: ', words);
        // this.keywords = this.keywords.concat(words);
        let summary = this.formData.value.summary;
        words.forEach((word) => summary = summary ? (summary + ' ' + word) : word);
        this.formData.patchValue({summary});
      },
      error: (err) => {
        this.listening = false;
        this.notifier.error(`Error Getting Speech ${err}`);
      },
      complete: () => {
        this.listening = false;
        this.logger.info('Recording complete');
      }
    });
  }

}
