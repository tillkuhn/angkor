import {ComponentFixture, TestBed} from '@angular/core/testing';

import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {MetricsComponent} from '@app/admin/metrics/metrics.component';
import {MatTableModule} from '@angular/material/table';
import {RouterTestingModule} from '@angular/router/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatLegacySnackBar} from '@angular/material/legacy-snack-bar';
import {MatLegacyDialog} from '@angular/material/legacy-dialog';

describe('MetricsComponent', () => {
  let component: MetricsComponent;
  let fixture: ComponentFixture<MetricsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      schemas: [
        CUSTOM_ELEMENTS_SCHEMA
      ],
      // Angular15 legacy hack
      providers: [{provide: MatLegacySnackBar, useValue: {}}, {provide: MatLegacyDialog, useValue: {}}],
      imports: [LoggerTestingModule, HttpClientTestingModule, MatIconTestingModule, MatSnackBarModule, MatTableModule, RouterTestingModule],
      declarations: [MetricsComponent],
      teardown: {destroyAfterEach: false}
    })
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MetricsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
