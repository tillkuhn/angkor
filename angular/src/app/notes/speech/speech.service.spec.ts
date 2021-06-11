import { TestBed } from '@angular/core/testing';

import { SpeechService } from './speech.service';
import {LoggerTestingModule} from 'ngx-logger/testing';

describe('SpeechService', () => {
  let service: SpeechService;

  beforeEach(() => {
    TestBed.configureTestingModule({imports: [LoggerTestingModule]});
    service = TestBed.inject(SpeechService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
