import {TestBed} from '@angular/core/testing';

import {LoadingService} from './loading.service';

describe('LoadingService', () => {
  let service: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({ teardown: { destroyAfterEach: false } });
    service = TestBed.inject(LoadingService);
  });

  it('should be created and emit true', () => {
    expect(service).toBeTruthy();
    service.setLoading(true);
    service.isLoading$.subscribe(status => {
      expect(status).toBeTruthy();
    });
  });

  it('should be created and emit false', () => {
    service.setLoading(false);
    service.isLoading$.subscribe(status => {
      expect(status).toBeFalsy();
    });
  });


});
