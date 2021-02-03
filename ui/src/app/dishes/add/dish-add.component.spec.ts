import { ComponentFixture, TestBed, waitForAsync } from '@angular/core/testing';

import {DishAddComponent} from './dish-add.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
// imports: [RouterTestingModule, LoggerTestingModule, HttpClientTestingModule]
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {MatCardModule} from '@angular/material/card';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {WebStorageModule, WebStorageService} from 'ngx-web-storage';
import {MatIconTestingModule} from '@angular/material/icon/testing';

describe('DishAddComponent', () => {
  let component: DishAddComponent;
  let fixture: ComponentFixture<DishAddComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [DishAddComponent],
      imports: [MatCardModule, MatIconTestingModule, RouterTestingModule, LoggerTestingModule, HttpClientTestingModule, FormsModule,
        ReactiveFormsModule, WebStorageService, WebStorageModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DishAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // todo fix 	Error: No value accessor for form control with name: 'areaCode'
  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
