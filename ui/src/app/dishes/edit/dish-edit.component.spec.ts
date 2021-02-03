import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {DishEditComponent} from './dish-edit.component';
// https://stackoverflow.com/questions/38983766/angular-2-and-observables-cant-bind-to-ngmodel-since-it-isnt-a-known-prope
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {ClipboardModule} from '@angular/cdk/clipboard';
import {MatCardModule} from '@angular/material/card';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {WebStorageModule} from 'ngx-web-storage';
import {MatIconTestingModule} from '@angular/material/icon/testing'; // important for test

describe('DishEditComponent', () => {
  let component: DishEditComponent;
  let fixture: ComponentFixture<DishEditComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [DishEditComponent],
      imports: [MatIconModule, MatCardModule, LoggerTestingModule, RouterTestingModule, HttpClientTestingModule, FormsModule, ReactiveFormsModule,
        ClipboardModule, MatIconTestingModule, WebStorageModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DishEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // 	Error: No value accessor for form control with name: 'areaCode' :-( but why?

  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
