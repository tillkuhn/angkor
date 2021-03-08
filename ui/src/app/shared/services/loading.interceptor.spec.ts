import { TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { HTTP_INTERCEPTORS } from '@angular/common/http';
import {LoadingInterceptor} from './loading.interceptor';
import {LoadingService} from './loading.service';

// Check out great https://alligator.io/angular/testing-http-interceptors/

describe(`LoadingInterceptor`, () => {
  let service: LoadingService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        /*DataService,*/
        {
          provide: HTTP_INTERCEPTORS,
          useClass: LoadingInterceptor,
          multi: true,
        },
      ],
    });

    httpMock =  TestBed.inject(HttpTestingController);
    service =  TestBed.inject(LoadingService);
  });

  it('should add an Authorization header', () => {
    service.isLoading$.subscribe(response => {
        expect(response).toBeFalsy();
    });
    // Todo test loading ...
    // service.getPosts().subscribe(response => {
    //   expect(response).toBeTruthy();
    // });
    // const httpRequest = httpMock.expectOne(`${service.ROOT_URL}/posts`);
    // expect(httpRequest.request.headers.has('Authorization')).toEqual(true);
  });

});
