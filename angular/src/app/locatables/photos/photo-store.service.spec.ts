import {TestBed} from '@angular/core/testing';

import {PhotoStoreService} from './photo-store.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {PostStoreService} from '@app/locatables/posts/post-store.service';
import {LoggerTestingModule} from 'ngx-logger/testing';

describe('PhotoStoreService', () => {
  let service: PhotoStoreService;

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
