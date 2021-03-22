import {ComponentFixture, TestBed} from '@angular/core/testing';

import {LinkInputComponent} from './link-input.component';
import {MAT_DIALOG_DATA, MatDialogModule, MatDialogRef} from '@angular/material/dialog';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {LayoutModule} from '@angular/cdk/layout';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {FormsModule} from '@angular/forms';
import {Link} from '../../domain/link';
import {MatButtonModule} from '@angular/material/button';

describe('LinkInputComponent', () => {
  let component: LinkInputComponent;
  let fixture: ComponentFixture<LinkInputComponent>;
  const data: Link = {
    linkUrl: 'http://bla.de',
    name: 'some link',
    id: '',
    mediaType: 'VIDEO'
  };

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LinkInputComponent ],
      providers: [
        {provide: MAT_DIALOG_DATA, useValue: data},
        {provide: MatDialogRef, useValue: {}}
      ],
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      imports: [MatIconTestingModule, FormsModule, LayoutModule, LoggerTestingModule, MatButtonModule, MatDialogModule ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LinkInputComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
