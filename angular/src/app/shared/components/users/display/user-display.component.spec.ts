import {ComponentFixture, TestBed} from '@angular/core/testing';

import {UserDisplayComponent} from './user-display.component';
import {HttpClientTestingModule} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {provideNgxWebstorage, withLocalStorage, withSessionStorage} from 'ngx-webstorage';

describe('UserDisplayComponent', () => {
  let component: UserDisplayComponent;
  let fixture: ComponentFixture<UserDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideNgxWebstorage(withLocalStorage(), withSessionStorage())],
    imports: [HttpClientTestingModule, LoggerTestingModule, RouterTestingModule],
    declarations: [UserDisplayComponent],
    teardown: { destroyAfterEach: false }
})
      .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(UserDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
