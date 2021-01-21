import {ComponentFixture, TestBed} from '@angular/core/testing';

import {DishDetailComponent} from './dish-detail.component';
import {RouterTestingModule} from '@angular/router/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatCardModule} from '@angular/material/card';
import {MatIconModule} from '@angular/material/icon';

describe('DishDetailComponent', () => {
  let component: DishDetailComponent;
  let fixture: ComponentFixture<DishDetailComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DishDetailComponent],
      imports: [MatIconModule, MatCardModule, RouterTestingModule, LoggerTestingModule, HttpClientTestingModule, MatDialogModule, MatSnackBarModule]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DishDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
