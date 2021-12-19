import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RadioComponent} from './radio.component';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {WebStorageModule} from 'ngx-web-storage';

describe('SongComponent', () => {
  let component: RadioComponent;
  let fixture: ComponentFixture<RadioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      declarations: [RadioComponent],
      imports: [LoggerTestingModule, RouterTestingModule,
        MatIconTestingModule, NoopAnimationsModule, HttpClientTestingModule, MatSnackBarModule, WebStorageModule],
      teardown: {destroyAfterEach: false}
    })
      .compileComponents();
  });
  beforeEach(() => {
    fixture = TestBed.createComponent(RadioComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
