import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TourDetailsComponent} from './tour-details.component';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {WebStorageModule} from 'ngx-web-storage';
import {Tour} from '@domain/location';
import {EntityType} from '@shared/domain/entities';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {LayoutModule} from '@angular/cdk/layout';
import {MatSelectModule} from '@angular/material/select';
import {MatInputModule} from '@angular/material/input';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MatButtonModule} from '@angular/material/button';

describe('TourDetailsComponent', () => {
  let component: TourDetailsComponent;
  let fixture: ComponentFixture<TourDetailsComponent>;
  const data: Tour = {
    id: '12356',
    name: 'tour',
    tags: [],
    primaryUrl: null,
    authScope: null,
    createdAt: null,
    createdBy: null,
    externalId: '12346',
    imageUrl: 'https://image',
    rating: 9,
    coordinates: [0, 0],
    entityType: EntityType.TOUR,
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      // Important to avoid No provider for InjectionToken MatDialogData
      providers: [
        {provide: MAT_DIALOG_DATA, useValue: data},
        {provide: MatDialogRef, useValue: {}}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      imports: [MatIconTestingModule, FormsModule, LayoutModule, WebStorageModule, MatSelectModule, MatFormFieldModule, MatInputModule,
        ReactiveFormsModule, NoopAnimationsModule, HttpClientTestingModule, RouterTestingModule, LoggerTestingModule,
        MatButtonModule, MatDialogModule],
      declarations: [TourDetailsComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TourDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
