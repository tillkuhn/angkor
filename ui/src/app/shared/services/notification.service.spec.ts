import {TestBed} from '@angular/core/testing';

import {NotificationService} from './notification.service';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {LoggerTestingModule} from 'ngx-logger/testing';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({imports: [MatSnackBarModule, LoggerTestingModule]});
    service = TestBed.inject(NotificationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(service.defaultCloseTitle).toContain('Got');
  });
});
