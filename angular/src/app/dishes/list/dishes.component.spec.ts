import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {DishesComponent} from './dishes.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {MatIconModule} from '@angular/material/icon';
import {MatCardModule} from '@angular/material/card';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {NgxWebstorageModule} from 'ngx-webstorage';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {MatTableModule} from '@angular/material/table';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {FormsModule} from '@angular/forms';


describe('DishesComponent', () => {
  let component: DishesComponent;
  let fixture: ComponentFixture<DishesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
    declarations: [DishesComponent],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ],
    imports: [MatIconModule, MatCardModule, RouterTestingModule, LoggerTestingModule, MatTabsModule,
        HttpClientTestingModule, MatIconTestingModule, MatSnackBarModule, NgxWebstorageModule.forRoot(), MatTableModule, FormsModule],
    teardown: { destroyAfterEach: false }
})
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DishesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
