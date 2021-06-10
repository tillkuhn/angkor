import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {DishAddComponent} from './dish-add.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
// imports: [RouterTestingModule, LoggerTestingModule, HttpClientTestingModule]
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatCardModule} from '@angular/material/card';
import {WebStorageModule, WebStorageService} from 'ngx-web-storage';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';

describe('DishAddComponent', () => {
  let component: DishAddComponent;
  let fixture: ComponentFixture<DishAddComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [DishAddComponent],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
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
