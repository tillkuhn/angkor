import { Injectable } from '@angular/core';
import {HttpClient, HttpEvent, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {EntityType} from '../domain/common';
import {ApiService} from './api.service';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private httpClient: HttpClient) { }

  // Todo move to dedicated service
  uploadFile(file: File, entityType: EntityType, entityId: string): Observable<HttpEvent<{}>> {
    const data: FormData = new FormData();
    data.append('file', file);
    const resourceApi =  '/imagine/upload/places';   // ApiService.getApiUrl(entityType);
    const newRequest = new HttpRequest('POST', `${resourceApi}/${entityId}`, data, {
      reportProgress: true,
      responseType: 'text'
    });
    return this.httpClient.request(newRequest);
  }

}
