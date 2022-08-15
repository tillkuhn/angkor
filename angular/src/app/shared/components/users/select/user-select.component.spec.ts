import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UserSelectComponent} from './user-select.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {WebStorageModule} from 'ngx-web-storage';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {UntypedFormControl} from '@angular/forms';

describe('UserSelectComponent', () => {
  let component: UserSelectComponent;
  let fixture: ComponentFixture<UserSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ],
    imports: [HttpClientTestingModule, LoggerTestingModule, RouterTestingModule, WebStorageModule],
    declarations: [UserSelectComponent],
    teardown: { destroyAfterEach: false }
})
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserSelectComponent);
    component = fixture.componentInstance;
    //component.formGroup = new FormBuilder().group({assignee: ['hase']});
    component.formControlSelect = new UntypedFormControl('');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
