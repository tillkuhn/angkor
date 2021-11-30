import {TestBed} from '@angular/core/testing';

import {GeoService} from './geo.service';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('GeoService', () => {
  let service: GeoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoggerTestingModule, HttpClientTestingModule, MatSnackBarModule]
    });
    service = TestBed.inject(GeoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
