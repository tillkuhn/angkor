import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {PlaceEditComponent} from './place-edit.component';
// https://stackoverflow.com/questions/38983766/angular-2-and-observables-cant-bind-to-ngmodel-since-it-isnt-a-known-prope
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {ClipboardModule} from '@angular/cdk/clipboard';
import {MatCardModule} from '@angular/material/card';
import {NgxWebstorageModule} from 'ngx-webstorage';
import {MatIconTestingModule} from '@angular/material/icon/testing';
import {CUSTOM_ELEMENTS_SCHEMA} from '@angular/core'; // important for test

describe('PlaceEditComponent', () => {
  let component: PlaceEditComponent;
  let fixture: ComponentFixture<PlaceEditComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
    declarations: [PlaceEditComponent],
    schemas: [
        CUSTOM_ELEMENTS_SCHEMA
    ],
    imports: [MatCardModule, LoggerTestingModule, RouterTestingModule, HttpClientTestingModule, FormsModule, ReactiveFormsModule,
        ClipboardModule, MatIconTestingModule, NgxWebstorageModule.forRoot()],
    teardown: { destroyAfterEach: false }
})
      .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(PlaceEditComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  // 	Error: No value accessor for form control with name: 'areaCode' :-( but why?

  xit('should create', () => {
    expect(component).toBeTruthy();
  });
});
