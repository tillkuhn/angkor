import {TestBed} from '@angular/core/testing';

import {TagService} from './tag.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('TagService', () => {
  let service: TagService;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [HttpClientTestingModule, LoggerTestingModule, MatSnackBarModule],
    teardown: { destroyAfterEach: false }
});
    service = TestBed.inject(TagService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
