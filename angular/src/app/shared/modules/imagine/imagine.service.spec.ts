import {TestBed} from '@angular/core/testing';

import {ImagineService} from './imagine.service';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {RouterTestingModule} from '@angular/router/testing';
import {WebStorageModule} from 'ngx-web-storage';
import {Authentication} from '@shared/services/auth.service';
import {FileItem} from '@shared/modules/imagine/file-item';
import {EntityType} from '@shared/domain/entities';

describe('FileService', () => {
  let service: ImagineService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, LoggerTestingModule, RouterTestingModule, WebStorageModule]
    });
    service = TestBed.inject(ImagineService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should convert entity type and id to a valid api path', () => {
    const path = ImagineService.getApiURL(EntityType.TOUR, '1345')
    expect(path).toBe('/imagine/tours/1345');
  });

  it('should return a file response', done => {
    const testId = 'd77321f4-1169-4ee1-9f8f-660cf099e8ef';
    const httpMock: HttpTestingController = TestBed.inject(HttpTestingController);
    const mockResponse: FileItem[] = [{filename: 'riverface.jpg'}];
    const authResponse: Authentication = {
      authenticated: true,
      user: {emoji: '🥺'},
      idToken: '123456'
    };

    const authRequest = httpMock.expectOne('/api/v1/authentication');
    authRequest.flush(authResponse);
    httpMock.expectOne('/api/v1/user-summaries').flush({});


    // service.checkAuthentication();
    service.getEntityFiles(EntityType.Place, testId)
      .subscribe((items: FileItem[]) => {
        expect(items).toBeTruthy();
        expect(items.length).toBe(mockResponse.length);
        expect(items[0].filename).toBe(mockResponse[0].filename);
        done(); // call the done function for async
      });
    const mockRequest = httpMock.expectOne(`/imagine/places/${testId}`);
    mockRequest.flush(mockResponse);
    // from https://skryvets.com/blog/2018/02/18/unit-testing-angular-service-with-httpclient/
    expect(mockRequest.request.method).toEqual('GET');
    expect(mockRequest.request.headers.get('X-Authorization')).toEqual(`Bearer ${authResponse.idToken}`);

  });

});
