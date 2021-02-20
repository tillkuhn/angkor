import {TestBed} from '@angular/core/testing';

import {LoadingService} from './loading.service';

fdescribe('LoadingService', () => {
  let service: LoadingService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(LoadingService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
    service.setLoading(true);
    service.isLoading$.subscribe(status => {
      expect(status).toBeTruthy();
    });
  });
});
