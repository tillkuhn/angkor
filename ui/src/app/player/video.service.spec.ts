import { TestBed } from '@angular/core/testing';

import {Video, VideoService} from './video.service';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {LoggerTestingModule} from 'ngx-logger/testing';

/**
 * https://blog.ordix.de/angular-unit-tests-einen-http-request-mocken-1
 */
describe('VideoService', () => {
  let service: VideoService;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule, LoggerTestingModule],
      providers: []
    });
    service = TestBed.inject(VideoService);
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

    service.getVideo$().subscribe((videos: Video[]) => {
      expect(videos).toBeTruthy();
      expect(videos[0].youtubeId).toBe(mockResponse[0].youtubeId);
      expect(videos[0].name).toBe(mockResponse[0].name);
      done(); // call the done function for async
    });

    const mockRequest = httpMock.expectOne(
      '/api/v1/videos'
    );
    mockRequest.flush(mockResponse);
  });

  });
