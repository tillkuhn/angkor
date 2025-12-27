import {ComponentFixture, TestBed} from '@angular/core/testing';

import {MyProfileComponent} from './my-profile.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {NgxWebstorageModule} from 'ngx-webstorage';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {
  MAT_DIALOG_DATA as MAT_DIALOG_DATA,
  MatDialog,
  MatDialogRef as MatDialogRef
} from '@angular/material/dialog';
import {MatSnackBar} from '@angular/material/snack-bar';

describe('UserProfileComponent', () => {
  let component: MyProfileComponent;
  let fixture: ComponentFixture<MyProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      // Angular15 Hack see legacy import
      providers: [
        {provide: MAT_DIALOG_DATA, useValue: {}},
        {provide: MatDialog, useValue: {}},
        {provide: MatSnackBar, useValue: {}}
      ],
      imports: [MatIconTestingModule, HttpClientTestingModule, LoggerTestingModule, NgxWebstorageModule.forRoot(), RouterTestingModule, MatDialogModule, MatSnackBarModule],
      declarations: [MyProfileComponent],
      teardown: {destroyAfterEach: false}
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MyProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
