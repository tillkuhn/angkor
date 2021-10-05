import {TestBed} from '@angular/core/testing';

import {Authentication, AuthService} from './auth.service';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {WebStorageModule} from 'ngx-web-storage';

describe('AuthService', () => {
  let service: AuthService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, LoggerTestingModule, RouterTestingModule, WebStorageModule]
    });
    service = TestBed.inject(AuthService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should be unauthenticated on startup', () => {
    expect(service.isAuthenticated).toBeFalsy();
  });

  it('should evaluate authenticated response properly', done => {
    const httpMock: HttpTestingController = TestBed.inject(HttpTestingController);
    const mockResponse: Authentication = {
      authenticated: true,
      user: {emoji: '🥺'},
      idToken: '123456'
    };

    const mockRequest = httpMock.expectOne(
      '/api/v1/authentication'
    );
    mockRequest.flush(mockResponse);
    // service.checkAuthentication();
    service.authentication$.subscribe((authentication: Authentication) => {
      expect(authentication).toBeTruthy();
      expect(authentication.idToken).toBe(mockResponse.idToken);
      expect(authentication.authenticated).toBeTruthy();
      done(); // call the done function for async
    });

  });

});
