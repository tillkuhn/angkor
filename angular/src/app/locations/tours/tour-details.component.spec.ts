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
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatFormFieldModule} from '@angular/material/form-field';
import {LayoutModule} from '@angular/cdk/layout';
import {MatSelectModule} from '@angular/material/select';
import {MatInputModule} from '@angular/material/input';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {MatButtonModule} from '@angular/material/button';
import {LocationDetailsComponent} from '@app/locations/location-details.component';

/**
 *
 * When using jest.fn() as described in https://stackoverflow.com/a/45309677/4292075
 * Make sure to add Type 'jest' to types array src/tsconfig.spec.json or Intellij will complain it cannot find name jest.
 * https://intellij-support.jetbrains.com/hc/en-us/community/posts/360000368020--Cannot-find-name-jest-Typescript-error?sort_by=created_at
 * https://stackoverflow.com/questions/45304270/jest-createspyobj
 *
 */

describe('TourDetailsComponent', () => {
  let component: TourDetailsComponent;
  let fixture: ComponentFixture<TourDetailsComponent>;
  // const func = jest.fn();
  // Unit test for @ViewChild approach
  // https://indepth.dev/posts/1415/implementing-reusable-and-reactive-forms-in-angular-2
  const formBuilder: FormBuilder = new FormBuilder();

  // Mock locationDetails (Child)
  // const locationDetails = createSpyObj('LocationDetailsComponent', ['createFormGroup', 'className', 'formData']);
  const locationDetails = new LocationDetailsComponent<Tour>(null, null, null, null, null);
  // locationDetails['className'] = jest.fn();

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
        {provide: MatDialogRef, useValue: {}},
        { provide: FormBuilder, useValue: formBuilder }, // for @ViewChild
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
    // for @ViewChild
    component.locationDetails = locationDetails;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


});
