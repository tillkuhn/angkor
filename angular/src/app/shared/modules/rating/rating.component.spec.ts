import {ComponentFixture, TestBed} from '@angular/core/testing';

import {RatingComponent} from './rating.component';
import {MatFormFieldModule} from '@angular/material/form-field';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatButtonModule} from '@angular/material/button';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';

describe('RatingComponent', () => {
  let component: RatingComponent;
  let fixture: ComponentFixture<RatingComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      declarations: [ RatingComponent ],
      imports: [MatFormFieldModule, LoggerTestingModule, MatButtonModule, NoopAnimationsModule],
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RatingComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create and initialize', () => {
    component.initialRating = 3
    component.starCount = 15;
    component.color = 'accent';
    component.ngOnInit();
    expect(component).toBeTruthy();
    expect(component.rating).toBe(3);
    expect(component.ratingArr.length).toBe(15);
    expect(component.color).toBe('accent');
    component.onClick(2);
    expect(component.rating).toBe(2);
    expect(component.showIcon(1)).toBe('star');
    expect(component.showIcon(15)).toBe('star_border');
  });
});
