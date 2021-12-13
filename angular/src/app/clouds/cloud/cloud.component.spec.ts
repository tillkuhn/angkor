import {ComponentFixture, TestBed} from '@angular/core/testing';

import {CloudComponent} from './cloud.component';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core';
import {MatIconTestingModule} from '@angular/material/icon/testing';

describe('CloudComponent', () => {
  let component: CloudComponent;
  let fixture: ComponentFixture<CloudComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ],
    imports: [LoggerTestingModule, HttpClientTestingModule, MatIconTestingModule],
    declarations: [CloudComponent],
    teardown: { destroyAfterEach: false }
})
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CloudComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
