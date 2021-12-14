import {TestBed} from '@angular/core/testing';

import {PlaceStoreService} from './place-store.service';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('PlaceStoreService', () => {
  let service: PlaceStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [LoggerTestingModule, HttpClientTestingModule, MatSnackBarModule],
    teardown: { destroyAfterEach: false }
});
    service = TestBed.inject(PlaceStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
