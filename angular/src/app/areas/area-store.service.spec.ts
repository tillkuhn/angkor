import {TestBed} from '@angular/core/testing';

import {AreaStoreService} from './area-store.service';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('AreaStoreService', () => {
  let service: AreaStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [LoggerTestingModule, HttpClientTestingModule, MatSnackBarModule],
    teardown: { destroyAfterEach: false }
});
    service = TestBed.inject(AreaStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
