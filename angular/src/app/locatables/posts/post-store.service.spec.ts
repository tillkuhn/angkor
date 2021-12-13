import {TestBed} from '@angular/core/testing';

import {PostStoreService} from './post-store.service';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('PostStoreService', () => {
  let service: PostStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [LoggerTestingModule, HttpClientTestingModule, MatSnackBarModule],
    teardown: { destroyAfterEach: false }
});
    service = TestBed.inject(PostStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
