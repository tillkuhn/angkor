import { Injectable } from '@angular/core';
import {HttpClient, HttpEvent, HttpRequest} from '@angular/common/http';
import {Observable} from 'rxjs';
import {environment} from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private httpClient: HttpClient) { }

  // Todo move to dedicated service
  uploadFile(file: File, entityId: string): Observable<HttpEvent<{}>> {
    const data: FormData = new FormData();
    data.append('file', file);
    const newRequest = new HttpRequest('POST', `${environment.apiUrlRoot}/places/${entityId}/upload`, data, {
      reportProgress: true,
      responseType: 'text'
    });
    return this.httpClient.request(newRequest);
  }

}
