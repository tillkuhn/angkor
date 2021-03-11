import {TestBed} from '@angular/core/testing';

import {EnvironmentService} from './environment.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';

describe('EnvironmentService', () => {
  let service: EnvironmentService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoggerTestingModule]
    });
    service = TestBed.inject(EnvironmentService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should display defaut "latest" for appVersion', () => {
    expect(service.appVersion).toEqual('latest');
  });

});
