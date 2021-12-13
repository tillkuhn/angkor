import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {PlaceDetailComponent} from './place-detail.component';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatDialogModule} from '@angular/material/dialog';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {DateFnsModule, FormatDistanceToNowPipeModule} from 'ngx-date-fns';
import {MatCardModule} from '@angular/material/card';
import {WebStorageModule} from 'ngx-web-storage';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {CUSTOM_ELEMENTS_SCHEMA, SecurityContext} from '@angular/core';
import {MarkdownModule} from 'ngx-markdown';

describe('PlaceDetailComponent', () => {
  let component: PlaceDetailComponent;
  let fixture: ComponentFixture<PlaceDetailComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
    declarations: [PlaceDetailComponent],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ],
    imports: [MatIconTestingModule, MatCardModule, RouterTestingModule, LoggerTestingModule, MatSnackBarModule,
        HttpClientTestingModule, MatDialogModule, MatSnackBarModule, DateFnsModule, WebStorageModule, FormatDistanceToNowPipeModule,
        // https://github.com/jfcere/ngx-markdown/blob/master/lib/src/markdown.service.spec.ts
        MarkdownModule.forRoot({ sanitize: SecurityContext.HTML })],
    teardown: { destroyAfterEach: false }
})
      .compileComponents();
  }));

  beforeEach(() => {
    // domSanitizer = TestBed.inject(DomSanitizer);
    // markdownService = TestBed.inject(MarkdownService);
    fixture = TestBed.createComponent(PlaceDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
