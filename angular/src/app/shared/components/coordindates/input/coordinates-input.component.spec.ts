import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CoordinatesInputComponent} from './coordinates-input.component';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {UntypedFormControl, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {RouterTestingModule} from '@angular/router/testing';
import {MatIconModule} from '@angular/material/icon';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatLegacyDialog} from '@angular/material/legacy-dialog';
import {MatLegacySnackBar} from '@angular/material/legacy-snack-bar';

describe('CoordinatesInputComponent', () => {
  let component: CoordinatesInputComponent;
  let fixture: ComponentFixture<CoordinatesInputComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      // Angular15
      providers: [{provide: MatLegacySnackBar, useValue: {}}],
      imports: [MatSnackBarModule, FormsModule, ReactiveFormsModule,
        RouterTestingModule, MatIconModule, LoggerTestingModule],
      declarations: [CoordinatesInputComponent],
      teardown: {destroyAfterEach: false}
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CoordinatesInputComponent);
    component = fixture.componentInstance;
    component.formControlInput = new UntypedFormControl('');
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
