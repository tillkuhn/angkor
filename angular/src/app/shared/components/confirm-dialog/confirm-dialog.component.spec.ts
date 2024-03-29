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
import {MAT_LEGACY_DIALOG_DATA as MAT_DIALOG_DATA, MatLegacyDialogRef as MatDialogRef} from '@angular/material/legacy-dialog';
import {MatCardModule} from '@angular/material/card';
import {NgxWebstorageModule} from 'ngx-webstorage';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {MatTableModule} from '@angular/material/table';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {ConfirmDialogComponent, ConfirmDialogModel} from './confirm-dialog.component';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {MatDatepickerModule} from '@angular/material/datepicker';
import {MatNativeDateModule} from '@angular/material/core';
// Angular15
import {MatLegacyDialogModule} from '@angular/material/legacy-dialog';

describe('ConfirmDialogComponent', () => {
  let component: ConfirmDialogComponent;
  let fixture: ComponentFixture<ConfirmDialogComponent>;
  const dialogData: ConfirmDialogModel = {
    message: 'Hi',
    title: 'Important'
  };

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ConfirmDialogComponent],
      // Angular15 Hack see legacy import
      providers: [
        {provide: MAT_DIALOG_DATA, useValue: dialogData},
        {provide: MatDialogRef, useValue: {}}
      ],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      imports: [MatIconTestingModule, MatCardModule, LayoutModule, LoggerTestingModule, RouterTestingModule,
        HttpClientTestingModule, MatTabsModule, MatTableModule, MatLegacyDialogModule,
        BrowserAnimationsModule, MatFormFieldModule, FormsModule, ReactiveFormsModule, MatSnackBarModule, MatInputModule,
        MatIconModule, NgxWebstorageModule.forRoot(), MatSelectModule, MatDatepickerModule, MatNativeDateModule],
      teardown: {destroyAfterEach: false}
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
