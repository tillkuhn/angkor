import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {NotesComponent} from './notes.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {LayoutModule} from '@angular/cdk/layout';
import {MatInputModule} from '@angular/material/input';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatIconModule} from '@angular/material/icon';
import {MatDialogModule} from '@angular/material/dialog';
import {MatCardModule} from '@angular/material/card';
import {WebStorageModule} from 'ngx-web-storage';
import {MatTabsModule} from '@angular/material/tabs';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {MatTableModule} from '@angular/material/table';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatLegacySnackBar} from '@angular/material/legacy-snack-bar';
import {MatLegacyDialog} from '@angular/material/legacy-dialog';


describe('NotesComponent', () => {
  let component: NotesComponent;
  let fixture: ComponentFixture<NotesComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [NotesComponent],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      // Angular15 legacy hack
      providers: [{provide: MatLegacySnackBar, useValue: {}},{ provide: MatLegacyDialog, useValue: {}}],
      imports: [MatIconTestingModule, MatCardModule, LayoutModule, LoggerTestingModule, RouterTestingModule,
        HttpClientTestingModule, MatDialogModule, MatTabsModule, MatTableModule,
        FormsModule, ReactiveFormsModule, MatSnackBarModule, MatInputModule,
        BrowserAnimationsModule, MatIconModule, WebStorageModule],
      teardown: {destroyAfterEach: false}
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NotesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
