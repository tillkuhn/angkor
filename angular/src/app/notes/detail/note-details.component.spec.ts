import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {LayoutModule} from '@angular/cdk/layout';
import {MatInputModule} from '@angular/material/input';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatIconModule} from '@angular/material/icon';
// import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
// With Angular15, MatDialog ist now MatLegacyDialog ... the update CLI updated the component class, but not
// the corresponding jest test, so we have to do it ourselves. Import of MatDialogModule in beforeEach seems to be no longer necessary
import {MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef} from '@angular/material/legacy-dialog';
import {MatCardModule} from '@angular/material/card';
import {NgxWebstorageModule} from 'ngx-webstorage';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {MatTableModule} from '@angular/material/table';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {NoteDetailsComponent} from './note-details.component';
import {Note} from '@app/domain/note';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatNativeDateModule} from '@angular/material/core';

describe('NoteDetailsComponent', () => {
  let component: NoteDetailsComponent;
  let fixture: ComponentFixture<NoteDetailsComponent>;
  const noteData: Note = {
    id: '12356',
    tags: [],
    status: 'OPEN',
    summary: null,
    assignee: null,
    primaryUrl: null,
    authScope: null,
    dueDate: null,
    createdAt: null,
    createdBy: null
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
    declarations: [NoteDetailsComponent],
    providers: [
        { provide: MAT_DIALOG_DATA, useValue: noteData },
        { provide: MatDialogRef, useValue: {} }
    ],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ],
    imports: [MatIconTestingModule, MatCardModule, LayoutModule, LoggerTestingModule, RouterTestingModule,
        HttpClientTestingModule,/* MatDialogModule,*/ MatTabsModule, MatTableModule,
      BrowserAnimationsModule, MatFormFieldModule, FormsModule, ReactiveFormsModule, MatSnackBarModule, MatInputModule,
        MatIconModule, NgxWebstorageModule.forRoot(), MatSelectModule, MatDatepickerModule, MatNativeDateModule],
    teardown: { destroyAfterEach: false }
})
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NoteDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('increment due date', () => {
    //     const dueDate = this.formData.value.dueDate;
    const currentDate = new Date();
    component.formData.value.dueDate = currentDate
    component.incrementDueDate(10);
    //console.log(component.formData.value);
    expect(component.formData.value.dueDate.getTime()).toBeGreaterThan(currentDate.getTime());
    component.formData.value.dueDate = null
    component.incrementDueDate(1);
    expect(component.formData.value.dueDate.getTime()).toBeGreaterThan(currentDate.getTime());
  });
});
