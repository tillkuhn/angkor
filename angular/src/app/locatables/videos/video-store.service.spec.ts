import {TestBed} from '@angular/core/testing';

import {VideoStoreService} from './video-store.service';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('VideoStoreService', () => {
  let service: VideoStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [LoggerTestingModule, HttpClientTestingModule, MatSnackBarModule],
    teardown: { destroyAfterEach: false }
});
    service = TestBed.inject(VideoStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
