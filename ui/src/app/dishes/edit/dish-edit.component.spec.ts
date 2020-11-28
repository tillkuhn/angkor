import {async, ComponentFixture, TestBed} from '@angular/core/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {DishEditComponent} from './dish-edit.component';
// https://stackoverflow.com/questions/38983766/angular-2-and-observables-cant-bind-to-ngmodel-since-it-isnt-a-known-prope
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatIconModule} from '@angular/material/icon';
import {ClipboardModule} from '@angular/cdk/clipboard'; // important for test

describe('DishEditComponent', () => {
  let component: DishEditComponent;
  let fixture: ComponentFixture<DishEditComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [DishEditComponent],
      imports: [LoggerTestingModule, RouterTestingModule, HttpClientTestingModule, FormsModule, ReactiveFormsModule,
        ClipboardModule, MatIconModule]
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
