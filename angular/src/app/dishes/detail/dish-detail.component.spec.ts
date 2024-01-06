import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DishDetailComponent} from './dish-detail.component';
import {RouterTestingModule} from '@angular/router/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatCardModule} from '@angular/material/card';
import {NgxWebstorageModule} from 'ngx-webstorage';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MarkdownModule} from 'ngx-markdown';
import {MatDatepickerModule} from '@angular/material/datepicker';
// Angular15 fix
import {MatLegacyDialog} from '@angular/material/legacy-dialog';

describe('DishDetailComponent', () => {
  let component: DishDetailComponent;
  let fixture: ComponentFixture<DishDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DishDetailComponent],
      providers: [{provide: MatLegacyDialog, useValue: {}}],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      imports: [MatIconTestingModule, MatCardModule, RouterTestingModule, LoggerTestingModule, HttpClientTestingModule, MatDialogModule,
        MatSnackBarModule, MarkdownModule, MatDatepickerModule, NgxWebstorageModule.forRoot()],
      teardown: {destroyAfterEach: false}

    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DishDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
