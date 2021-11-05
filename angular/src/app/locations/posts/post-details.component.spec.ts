import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PostDetailsComponent } from './post-details.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {LayoutModule} from '@angular/cdk/layout';
import {WebStorageModule} from 'ngx-web-storage';
import {MatSelectModule} from '@angular/material/select';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatInputModule} from '@angular/material/input';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {MatButtonModule} from '@angular/material/button';
import {Link} from '@domain/link';

describe('PostDetailsComponent', () => {
  let component: PostDetailsComponent;
  let fixture: ComponentFixture<PostDetailsComponent>;

  // todo real post
  const data: Link = {
    linkUrl: 'https://post.de',
    name: 'some link',
    id: '',
    mediaType: 'DEFAULT'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PostDetailsComponent],
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
    fixture = TestBed.createComponent(PostDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
