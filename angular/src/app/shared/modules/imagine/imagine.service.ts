import {Injectable} from '@angular/core';
import {HttpClient, HttpEvent, HttpHeaders, HttpRequest} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {EntityType} from '@shared/domain/entities';
import {environment} from '../../../../environments/environment';
import {catchError, tap} from 'rxjs/operators';
import {FileItem, FileUpload} from '@shared/modules/imagine/file-item';
import {NGXLogger} from 'ngx-logger';
import {ApiHelper} from '../../helpers/api-helper';
import {AuthService} from '@shared/services/auth.service';

@Injectable({
  providedIn: 'root'
})
export class ImagineService {

  constructor(private http: HttpClient,
              private authService: AuthService,
              private logger: NGXLogger) {
  }

  // upload file as multipart
  uploadFile(file: File, entityType: EntityType, entityId: string): Observable<HttpEvent<{}>> {
    const data: FormData = new FormData();
    const entityPath = ApiHelper.getApiPath(entityType); // e.g. places
    data.append('uploadfile', file); // this must match the name in the multiform
    const newRequest = new HttpRequest('POST', `${environment.apiUrlImagine}/${entityPath}/${entityId}`, data, {
      reportProgress: true,
      responseType: 'text',
      headers: this.getHeaders()
    });
    return this.http.request(newRequest);
  }

  // Upload file via JSON Post request
  uploadUrl(fileUpload: FileUpload, entityType: EntityType, entityId: string): Observable<any> {
    const entityPath = ApiHelper.getApiPath(entityType); // e.g. places
    return this.http.post<FileUpload>(`${environment.apiUrlImagine}/${entityPath}/${entityId}`, fileUpload, {headers: this.getHeaders()})
      .pipe(
        tap((result: any) => this.logger.debug(`FileService.uploadUrl added file upload result ${result}`)),
        catchError(this.handleError<FileUpload>('addFileUpload'))
      );
  }

  getEntityFiles(entityType: EntityType, entityId: string): Observable<FileItem[]> {
    const entityPath = ApiHelper.getApiPath(entityType); // e.g. places
    return this.http.get<FileItem[]>(`${environment.apiUrlImagine}/${entityPath}/${entityId}`, {headers: this.getHeaders()})
      .pipe(
        tap(files => this.logger.debug(`FileService.getEntityFiles for ${entityId}`)),
        catchError(this.handleError('getFiles', []))
      );
  }

  // todo centralize
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      this.logger.error(`${operation}: ${error}`); // log to console instead
      // Let the app keep running by returning an empty result. ???
      return of(result); // as T is unnecessary since it does not change the type of expression (sonarlint)
    };
  }

  /**
   * If user is logged in and JWT is present in AuthService,
   * Provide it to imagine service as Bearer token in X-Authorization header
   */
  private getHeaders(): HttpHeaders {
    const idToken = this.authService.idToken;
    if (idToken) {
      return new HttpHeaders()
        .set('X-Authorization', `Bearer ${idToken}`);
    } else {
      return new HttpHeaders();
    }
  }

}
