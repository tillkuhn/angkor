import {ComponentFixture, TestBed} from '@angular/core/testing';

import {AreaTreeComponent} from './area-tree.component';
import {RouterTestingModule} from '@angular/router/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatIconModule} from '@angular/material/icon';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatInputModule} from '@angular/material/input';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatSelectModule} from '@angular/material/select';
import {MatCardModule} from '@angular/material/card';
import {WebStorageModule } from 'ngx-web-storage';
import {MatIconTestingModule} from '@angular/material/icon/testing';

describe('AreaTreeComponent', () => {
  let component: AreaTreeComponent;
  let fixture: ComponentFixture<AreaTreeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [AreaTreeComponent],
      imports: [MatIconTestingModule, MatCardModule, RouterTestingModule, LoggerTestingModule, HttpClientTestingModule, MatIconModule, MatSelectModule,
        FormsModule, ReactiveFormsModule, MatSnackBarModule, MatInputModule, BrowserAnimationsModule, WebStorageModule]
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
