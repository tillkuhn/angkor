import { TestBed } from '@angular/core/testing';

import { DishStoreService } from './dish-store.service';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('DishStoreService', () => {
  let service: DishStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({imports: [LoggerTestingModule, HttpClientTestingModule, MatSnackBarModule]});
    service = TestBed.inject(DishStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
