import {TestBed} from '@angular/core/testing';

import {MasterDataService} from './master-data.service';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';

describe('MasterDataService', () => {
  let service: MasterDataService;

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [HttpClientTestingModule, LoggerTestingModule, MatSnackBarModule],
    teardown: { destroyAfterEach: false }
});
    service = TestBed.inject(MasterDataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(service.getLocationTypes().length).toBeGreaterThan(0);
  });
});
