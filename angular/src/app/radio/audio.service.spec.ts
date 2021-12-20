import { TestBed } from '@angular/core/testing';

import { AudioService } from './audio.service';

describe('AudioService', () => {
  let service: AudioService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AudioService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should format seconds to mm:ss', () => {
    expect(service.formatTime(30)).toEqual('00:30');
    expect(service.formatTime(330)).toEqual('05:30');
  });
});
