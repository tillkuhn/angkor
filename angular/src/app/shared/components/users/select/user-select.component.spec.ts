import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UserSelectComponent} from './user-select.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {LocalStorageService, provideNgxWebstorage, withLocalStorage, withSessionStorage} from 'ngx-webstorage';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {UntypedFormControl} from '@angular/forms';

describe('UserSelectComponent', () => {
  let component: UserSelectComponent;
  let fixture: ComponentFixture<UserSelectComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideNgxWebstorage(withLocalStorage(), withSessionStorage())],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ],
    imports: [HttpClientTestingModule, LoggerTestingModule, RouterTestingModule],
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
