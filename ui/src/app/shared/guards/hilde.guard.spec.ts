import { TestBed } from '@angular/core/testing';

import { HildeGuard } from './hilde.guard';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {WebStorageModule} from 'ngx-web-storage';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';

describe('AuthGuard', () => {
  let guard: HildeGuard;

  beforeEach(() => {
    TestBed.configureTestingModule({imports: [WebStorageModule, LoggerTestingModule, RouterTestingModule]});
    guard = TestBed.inject(HildeGuard);
  });

  it('should be created', () => {
    expect(guard).toBeTruthy();
  });
});
