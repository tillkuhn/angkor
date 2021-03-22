import {TestBed} from '@angular/core/testing';

import {LinkStoreService} from './link-store.service';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {Link} from '../domain/link';

describe('LinkStoreService', () => {
  let service: LinkStoreService;

  beforeEach(() => {
    TestBed.configureTestingModule({imports: [LoggerTestingModule, HttpClientTestingModule, MatSnackBarModule]});
    service = TestBed.inject(LinkStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });


  it('should perform a mocked http request', done  => {
    const httpMock: HttpTestingController = TestBed.inject(HttpTestingController);
    const mockResponse = [
      {
        youtubeId: '123456',
        name: 'Ready to watch',
      }];

    service.getVideo$().subscribe((videos: Link[]) => {
      expect(videos).toBeTruthy();
      expect(videos[0].youtubeId).toBe(mockResponse[0].youtubeId);
      expect(videos[0].name).toBe(mockResponse[0].name);
      done(); // call the done function for async
    });

    const mockRequest = httpMock.expectOne(
      '/api/v1/links/videos'
    );
    mockRequest.flush(mockResponse);
  });

});
