import {TestBed} from '@angular/core/testing';

import {TourStoreService} from './tour-store.service';
import {LoggerTestingModule} from 'ngx-logger/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {Tour} from '@domain/location';

describe('TourStoreService', () => {
  let service: TourStoreService;
  const testId = '2021-01-01';
  const mockResponse = [
    {
      id: testId,
      name: 'Ready to mock',
    }];

  beforeEach(() => {
    TestBed.configureTestingModule({
    imports: [LoggerTestingModule, HttpClientTestingModule, MatSnackBarModule],
    teardown: { destroyAfterEach: false }
});
    service = TestBed.inject(TourStoreService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should perform a single get requests', done => {
    const httpMock: HttpTestingController = TestBed.inject(HttpTestingController);

    service.getItem(testId).subscribe((item: Tour) => {
      expect(item).toBeTruthy();
      expect(item.id).toBe(mockResponse[0].id);
      expect(item.name).toBe(mockResponse[0].name);
      done(); // call the done function for async
    });

    const mockRequestSingle = httpMock.expectOne(`/api/v1/tours/${testId}`);
    mockRequestSingle.flush(mockResponse[0]);

  });

  it('should perform a search requests', done => {
    const httpMock: HttpTestingController = TestBed.inject(HttpTestingController);

    service.searchItems().subscribe((items: Tour[]) => {
      expect(items).toBeTruthy();
      expect(items[0].id).toBe(mockResponse[0].id);
      expect(items[0].name).toBe(mockResponse[0].name);
      done(); // call the done function for async
    });
    const mockRequestList = httpMock.expectOne('/api/v1/tours/search');
    mockRequestList.flush(mockResponse);

  });

});
