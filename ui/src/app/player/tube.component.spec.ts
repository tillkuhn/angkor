import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TubeComponent } from './tube.component';
import {YouTubePlayerModule} from '@angular/youtube-player';
import {FormsModule} from '@angular/forms';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {MatFormFieldModule} from '@angular/material/form-field';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {MatSelectModule} from '@angular/material/select';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';

describe('YoutubePlayerDemoComponent', () => {
  let component: TubeComponent;
  let fixture: ComponentFixture<TubeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      declarations: [ TubeComponent ],
      imports: [ YouTubePlayerModule, FormsModule, LoggerTestingModule, MatFormFieldModule, RouterTestingModule,
        MatIconTestingModule, MatSelectModule, NoopAnimationsModule, HttpClientTestingModule]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TubeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
