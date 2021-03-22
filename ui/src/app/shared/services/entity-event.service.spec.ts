import { TestBed } from '@angular/core/testing';

import { EntityEventService } from './entity-event.service';

describe('EntityEventService', () => {
  let service: EntityEventService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(EntityEventService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
