import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {MapComponent} from './map.component';

import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {WebStorageModule} from 'ngx-web-storage';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {NgxMapboxGLModule} from 'ngx-mapbox-gl';
// imports: [RouterTestingModule, LoggerTestingModule, HttpClientTestingModule]

describe('MapComponent', () => {
  let component: MapComponent;
  let fixture: ComponentFixture<MapComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [MapComponent],
      imports: [MatIconTestingModule, RouterTestingModule, LoggerTestingModule, HttpClientTestingModule, MatSnackBarModule,
        WebStorageModule, NgxMapboxGLModule]
    })
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MapComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
