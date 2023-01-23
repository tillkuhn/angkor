import {TestBed, waitForAsync} from '@angular/core/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {AppComponent} from './app.component';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {WebStorageModule} from 'ngx-web-storage';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatLegacySnackBar} from '@angular/material/legacy-snack-bar';
import {MatLegacyDialog} from '@angular/material/legacy-dialog';

describe('AppComponent', () => {
  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
      // Angular15 legacy hack
      providers: [{provide: MatLegacySnackBar, useValue: {}}, {provide: MatLegacyDialog, useValue: {}}],
      imports: [
        RouterTestingModule, MatSnackBarModule, LoggerTestingModule, HttpClientTestingModule,
        WebStorageModule, MatIconTestingModule
      ],
      declarations: [
        AppComponent
      ],
      teardown: {destroyAfterEach: false}
    }).compileComponents();
  }));

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app).toBeTruthy();
  });

  it(`should have as title 'TiMaFe on Air'`, () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.debugElement.componentInstance;
    expect(app.title).toEqual('TiMaFe on Air');
  });

  /*
  it('should render title in a h1 tag', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.debugElement.nativeElement;
    expect(compiled.querySelector('h1').textContent).toContain('Welcome to angular8-crud!');
  });
   */
});
