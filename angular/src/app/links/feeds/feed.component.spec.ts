import {ComponentFixture, TestBed} from '@angular/core/testing';

import {FeedComponent} from './feed.component';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {MatSelectModule} from '@angular/material/select';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {NgxWebstorageModule} from 'ngx-webstorage';

describe('FeedComponent', () => {
  let component: FeedComponent;
  let fixture: ComponentFixture<FeedComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      declarations: [FeedComponent],
      imports: [LoggerTestingModule, RouterTestingModule,
        MatIconTestingModule, MatSelectModule, NoopAnimationsModule, HttpClientTestingModule, MatSnackBarModule, NgxWebstorageModule.forRoot()],
      teardown: {destroyAfterEach: false}
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FeedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
