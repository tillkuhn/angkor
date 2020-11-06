import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AreaTreeComponent } from './area-tree.component';
import {RouterTestingModule} from '@angular/router/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatIconModule} from '@angular/material/icon';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatInputModule} from '@angular/material/input';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';

describe('AreaTreeComponent', () => {
  let component: AreaTreeComponent;
  let fixture: ComponentFixture<AreaTreeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AreaTreeComponent ],
      imports: [RouterTestingModule, LoggerTestingModule, HttpClientTestingModule, MatIconModule,
        FormsModule, ReactiveFormsModule, MatSnackBarModule, MatInputModule, BrowserAnimationsModule]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AreaTreeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
