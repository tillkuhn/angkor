import {TestBed} from '@angular/core/testing';

import {NoteStoreService} from './note-store.service';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('NoteStoreService', () => {
  let service: NoteStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [LoggerTestingModule, HttpClientTestingModule, MatSnackBarModule],
    teardown: { destroyAfterEach: false }
});
    service = TestBed.inject(NoteStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
