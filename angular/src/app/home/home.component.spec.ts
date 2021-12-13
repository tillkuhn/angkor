import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {HomeComponent} from './home.component';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatIconModule} from '@angular/material/icon';
import {MatCardModule} from '@angular/material/card';
import {WebStorageModule} from 'ngx-web-storage';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {DateFnsModule} from 'ngx-date-fns';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
    declarations: [HomeComponent],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ],
    imports: [MatIconTestingModule, MatCardModule, LoggerTestingModule, HttpClientTestingModule, MatIconModule,
        RouterTestingModule, WebStorageModule, DateFnsModule, MatSnackBarModule],
    teardown: { destroyAfterEach: false }
})
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
