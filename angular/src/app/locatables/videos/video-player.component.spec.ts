import {ComponentFixture, TestBed} from '@angular/core/testing';

import {VideoPlayerComponent} from './video-player.component';
import {YouTubePlayerModule} from '@angular/youtube-player';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {WebStorageModule} from 'ngx-web-storage';
import {MatDialogModule} from '@angular/material/dialog';

describe('YoutubePlayerDemoComponent', () => {
  let component: VideoPlayerComponent;
  let fixture: ComponentFixture<VideoPlayerComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      declarations: [VideoPlayerComponent],
      imports: [YouTubePlayerModule, LoggerTestingModule, RouterTestingModule,
        MatIconTestingModule, NoopAnimationsModule, HttpClientTestingModule,
        MatSnackBarModule, WebStorageModule, MatDialogModule]
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VideoPlayerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
