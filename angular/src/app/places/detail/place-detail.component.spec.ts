import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {PlaceDetailComponent} from './place-detail.component';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
// Angular15
import {MatLegacyDialogModule} from '@angular/material/legacy-dialog';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatCardModule} from '@angular/material/card';
import {NgxWebstorageModule} from 'ngx-webstorage';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {CUSTOM_ELEMENTS_SCHEMA, SecurityContext} from '@angular/core';
import {MarkdownModule} from 'ngx-markdown';
import {HumanizeDatePipe} from '@shared/pipes/humanize-date.pipe';

describe('PlaceDetailComponent', () => {
  let component: PlaceDetailComponent;
  let fixture: ComponentFixture<PlaceDetailComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
    declarations: [PlaceDetailComponent, HumanizeDatePipe],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ],
    imports: [MatIconTestingModule, MatCardModule, RouterTestingModule, LoggerTestingModule, MatSnackBarModule,
        HttpClientTestingModule, MatLegacyDialogModule, MatSnackBarModule, NgxWebstorageModule.forRoot(),
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
