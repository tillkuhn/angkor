import {TestBed} from '@angular/core/testing';

import {EntityEventService} from './entity-event.service';
import {LoggerTestingModule} from 'ngx-logger/testing';

describe('EntityEventService', () => {
  let service: EntityEventService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [LoggerTestingModule]
    });
    service = TestBed.inject(EntityEventService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
