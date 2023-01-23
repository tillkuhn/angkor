import {TestBed} from '@angular/core/testing';

import {NotificationService} from './notification.service';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {MatLegacySnackBar} from '@angular/material/legacy-snack-bar';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      // Angular15 Hack
      providers: [{provide: MatLegacySnackBar, useValue: {}}],
      imports: [MatSnackBarModule, LoggerTestingModule],
      teardown: {destroyAfterEach: false}
    });
    service = TestBed.inject(NotificationService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
    expect(service.defaultCloseTitle).toContain('Got');
  });
});
