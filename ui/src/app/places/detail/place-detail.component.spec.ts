import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {PlaceDetailComponent} from './place-detail.component';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {DateFnsModule} from 'ngx-date-fns';
import {MatCardModule} from '@angular/material/card';

describe('PlaceDetailComponent', () => {
  let component: PlaceDetailComponent;
  let fixture: ComponentFixture<PlaceDetailComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [PlaceDetailComponent],
      imports: [MatCardModule, RouterTestingModule, LoggerTestingModule, MatSnackBarModule,
        HttpClientTestingModule, MatDialogModule, MatSnackBarModule, DateFnsModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlaceDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
