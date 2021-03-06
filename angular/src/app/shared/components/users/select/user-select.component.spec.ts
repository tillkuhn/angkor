import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UserSelectComponent} from './user-select.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {WebStorageModule} from 'ngx-web-storage';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {FormControl} from '@angular/forms';

describe('UserSelectComponent', () => {
  let component: UserSelectComponent;
  let fixture: ComponentFixture<UserSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      imports: [HttpClientTestingModule, LoggerTestingModule, RouterTestingModule, WebStorageModule], // all required for authservice
      declarations: [ UserSelectComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserSelectComponent);
    component = fixture.componentInstance;
    //component.formGroup = new FormBuilder().group({assignee: ['hase']});
    component.formControlSelect = new FormControl('');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
