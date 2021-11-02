import {ComponentFixture, TestBed} from '@angular/core/testing';

import {VideoDetailsComponent} from './video-details.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {LayoutModule} from '@angular/cdk/layout';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {Link} from '@app/domain/link';
import {MatButtonModule} from '@angular/material/button';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {WebStorageModule} from 'ngx-web-storage';
import {MatSelectModule} from '@angular/material/select';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';

describe('LinkInputComponent', () => {
  let component: VideoDetailsComponent;
  let fixture: ComponentFixture<VideoDetailsComponent>;
  const data: Link = {
    linkUrl: 'http://bla.de',
    name: 'some link',
    id: '',
    mediaType: 'VIDEO'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [VideoDetailsComponent],
      providers: [
        {provide: MAT_DIALOG_DATA, useValue: data},
        {provide: MatDialogRef, useValue: {}}
      ],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      imports: [MatIconTestingModule, FormsModule, LayoutModule, WebStorageModule, MatSelectModule, MatFormFieldModule, MatInputModule,
        ReactiveFormsModule, NoopAnimationsModule, HttpClientTestingModule, RouterTestingModule, LoggerTestingModule,
        MatButtonModule, MatDialogModule]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VideoDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
