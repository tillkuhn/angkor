import {ComponentFixture, TestBed} from '@angular/core/testing';

import {EventsComponent} from './events.component';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {MatTableModule} from '@angular/material/table';
import {DateFnsModule} from 'ngx-date-fns';

describe('EventsComponent', () => {
  let component: EventsComponent;
  let fixture: ComponentFixture<EventsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      imports: [LoggerTestingModule, HttpClientTestingModule, MatIconTestingModule, MatTableModule, DateFnsModule],
      declarations: [EventsComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EventsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
