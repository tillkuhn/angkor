import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CoordinatesInputComponent } from './coordinates-input.component';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {FormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {RouterTestingModule} from '@angular/router/testing';
import {MatIconModule} from '@angular/material/icon';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';

describe('CoordinatesInputComponent', () => {
  let component: CoordinatesInputComponent;
  let fixture: ComponentFixture<CoordinatesInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      imports: [MatSnackBarModule, FormsModule, ReactiveFormsModule,
        RouterTestingModule, MatIconModule, LoggerTestingModule],
      declarations: [ CoordinatesInputComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CoordinatesInputComponent);
    component = fixture.componentInstance;
    component.formControlInput = new FormControl('');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
