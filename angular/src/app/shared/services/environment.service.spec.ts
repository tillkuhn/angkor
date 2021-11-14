import {TestBed} from '@angular/core/testing';

import {EnvironmentService} from './environment.service';
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

  it('should display default "latest" for appVersion', () => {
    expect(service.appVersion).toEqual('latest');
  });

});
