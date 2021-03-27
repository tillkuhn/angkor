import {ComponentFixture, TestBed} from '@angular/core/testing';

import {LinkDetailsComponent} from './link-details.component';
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

describe('LinkInputComponent', () => {
  let component: LinkDetailsComponent;
  let fixture: ComponentFixture<LinkDetailsComponent>;
  const data: Link = {
    linkUrl: 'http://bla.de',
    name: 'some link',
    id: '',
    mediaType: 'VIDEO'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LinkDetailsComponent ],
      providers: [
        {provide: MAT_DIALOG_DATA, useValue: data},
        {provide: MatDialogRef, useValue: {}}
      ],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      imports: [MatIconTestingModule, FormsModule, LayoutModule, WebStorageModule,
        HttpClientTestingModule, RouterTestingModule, LoggerTestingModule, MatButtonModule, MatDialogModule, ReactiveFormsModule ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LinkDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
