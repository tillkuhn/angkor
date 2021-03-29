import {TestBed} from '@angular/core/testing';

import {ImagineService} from './imagine.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';

describe('FileService', () => {
  let service: ImagineService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, LoggerTestingModule]
    });
    service = TestBed.inject(ImagineService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
