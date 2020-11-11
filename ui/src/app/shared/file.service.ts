import { Injectable } from '@angular/core';
import {HttpClient, HttpEvent, HttpRequest} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {EntityType} from '../domain/common';
import {environment} from '../../environments/environment';
import {catchError, tap} from 'rxjs/operators';
import {FileItem} from '../domain/file-item';
import {NGXLogger} from 'ngx-logger';

@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private http: HttpClient,
              private logger: NGXLogger) { }

  // Todo move to dedicated service
  uploadFile(file: File, entityType: EntityType, entityId: string): Observable<HttpEvent<{}>> {
    const data: FormData = new FormData();
    data.append('uploadfile', file); // this must match the name in the multiform
    const newRequest = new HttpRequest('POST', `${environment.apiUrlImagine}/upload/places/${entityId}`, data, {
      reportProgress: true,
      responseType: 'text'
    });
    return this.http.request(newRequest);
  }


  getEntityFiles( entityType: EntityType, entityId: string): Observable<FileItem[]> {
    return this.http.get<FileItem[]>(`${environment.apiUrlImagine}/list/places/${entityId}`)
      .pipe(
        tap(files => this.logger.debug('ApiService fetched files')),
        catchError(this.handleError('getFiles', []))
      );
  }

  // todo centralize
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      this.logger.error(error); // log to console instead

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }


}
