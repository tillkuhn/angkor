import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {PlaceAddComponent} from './place-add.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
// imports: [RouterTestingModule, LoggerTestingModule, HttpClientTestingModule]
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {MatCardModule} from '@angular/material/card';
import {WebStorageModule} from 'ngx-web-storage';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';

describe('PlaceAddComponent', () => {
  let component: PlaceAddComponent;
  let fixture: ComponentFixture<PlaceAddComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
    declarations: [PlaceAddComponent],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ],
    imports: [MatIconTestingModule, MatCardModule, RouterTestingModule, LoggerTestingModule,
        HttpClientTestingModule, FormsModule, ReactiveFormsModule, MatIconModule, WebStorageModule],
    teardown: { destroyAfterEach: false }
})
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlaceAddComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // todo fix 	Error: No value accessor for form control with name: 'areaCode'
  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
