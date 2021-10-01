import { TestBed } from '@angular/core/testing';

import { TourStoreService } from './tour-store.service';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('TourStoreService', () => {
  let service: TourStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({imports: [LoggerTestingModule, HttpClientTestingModule, MatSnackBarModule]});
    service = TestBed.inject(TourStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
