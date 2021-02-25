import {TestBed} from '@angular/core/testing';

import {ApiService} from './api.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('ApiService', () => {

  beforeEach(() => TestBed.configureTestingModule({
    imports: [HttpClientTestingModule, LoggerTestingModule, MatSnackBarModule]
  }));

  it('should be created', () => {
    const service: ApiService = TestBed.get(ApiService);
    expect(service).toBeTruthy();
  });
});
