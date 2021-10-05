import {ComponentFixture, inject, TestBed} from '@angular/core/testing';

import {TagInputComponent} from './tag-input.component';
import {FormBuilder, FormsModule, ReactiveFormsModule, Validators} from '@angular/forms';
import {LayoutModule} from '@angular/cdk/layout';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatAutocompleteModule} from '@angular/material/autocomplete';
import {MatInputModule} from '@angular/material/input';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {EntityType} from '../../domain/entities';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatChipsModule} from '@angular/material/chips';
import {MatSnackBarModule} from '@angular/material/snack-bar';

// GREAT GREAT GREAT
// https://stackoverflow.com/questions/49162404/mocking-a-parent-formgroup-via-input-in-jasmine
describe('TagInputComponent', () => {
  let component: TagInputComponent;
  let fixture: ComponentFixture<TagInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA // Oh Yes this avoids *a lot of* warnings during test run
      ],
      imports: [FormsModule, ReactiveFormsModule, LayoutModule, LoggerTestingModule, MatSnackBarModule,
        MatAutocompleteModule, MatInputModule, NoopAnimationsModule, HttpClientTestingModule, MatChipsModule],
      declarations: [TagInputComponent]
    })
      .compileComponents();
  });

  beforeEach(inject([FormBuilder], (fb: FormBuilder) => {
    fixture = TestBed.createComponent(TagInputComponent);
    component = fixture.componentInstance;

    /* This is where we can simulate / test our component
       and pass in a value for formGroup where it would've otherwise
       required it from the parent
    */
    component.entityType = EntityType.Note;
    component.parentForm = fb.group({
      tags: ['', Validators.required]
    });
    fixture.detectChanges();
  }));
  it('should create', () => {
    expect(component).toBeTruthy();
  });

});
