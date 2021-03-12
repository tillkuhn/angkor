import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AreaDisplayComponent} from './area-display.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {WebStorageModule} from 'ngx-web-storage';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('AreaDisplayComponent', () => {
  let component: AreaDisplayComponent;
  let fixture: ComponentFixture<AreaDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, LoggerTestingModule, RouterTestingModule, MatSnackBarModule], // all required for authservice
      declarations: [AreaDisplayComponent]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AreaDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
