import {Injectable} from '@angular/core';
import {HttpClient, HttpEvent, HttpHeaders, HttpRequest} from '@angular/common/http';
import {Observable, of} from 'rxjs';
import {EntityMetadata, EntityType} from '@shared/domain/entities';
import {environment} from '../../../../environments/environment';
import {catchError, tap} from 'rxjs/operators';
import {FileItem, FileUpload, FileUrl} from '@shared/modules/imagine/file-item';
import {NGXLogger} from 'ngx-logger';
import {AuthService} from '@shared/services/auth.service';

/**
 * Rest Client for dedicated Imagine Service
 */
@Injectable({
  providedIn: 'root'
})
export class ImagineService {

  protected readonly className = 'ImagineService';

  /** getApiURL returns the full URL path to access all objects attached to a specific entity */
  static getApiURL(entityType: EntityType, entityId: string = null): string {
    const entityPath = EntityMetadata[entityType].path; // e.g. places, dishes ...
    return entityId ? `${environment.apiUrlImagine}/${entityPath}/${entityId}` : `${environment.apiUrlImagine}/${entityPath}`;
  }

  constructor(private http: HttpClient,
              private authService: AuthService,
              private logger: NGXLogger) {
  }

  /** upload Blob File as multipart */
  uploadFile(file: File, entityType: EntityType, entityId: string): Observable<HttpEvent<{}>> {
    const data: FormData = new FormData();
    data.append('uploadfile', file); // this must match the name in the multiform
    const newRequest = new HttpRequest('POST', ImagineService.getApiURL(entityType, entityId), data, {
      reportProgress: true,
      responseType: 'text',
      headers: this.getHeaders()
    });
    return this.http.request(newRequest);
  }

  /** Upload file via JSON Post request */
  uploadUrl(fileUpload: FileUpload, entityType: EntityType, entityId: string): Observable<any> {
    return this.http.post<FileUpload>(ImagineService.getApiURL(entityType, entityId), fileUpload, {headers: this.getHeaders()})
      .pipe(
        tap((result: any) => this.logger.debug(`${this.className}: FileService.uploadUrl added file upload result ${result}`)),
        catchError(this.handleError<FileUpload>('addFileUpload'))
      );
  }

  /** Returns a list of FileItems for the EntityType, usually filtered by a concrete EntityId*/
  getEntityFiles(entityType: EntityType, entityId: string = null): Observable<FileItem[]> {
    return this.http.get<FileItem[]>(ImagineService.getApiURL(entityType, entityId), {headers: this.getHeaders()})
      .pipe(
        tap(_ => this.logger.debug(`${this.className}.getEntityFiles: for ${entityId ? entityId : '<empty>'}`)),
        catchError(this.handleError('getEntityFiles', []))
      );
  }

  /** Returns a list commonPrefixes aka "Folders" from Service */
  getEntityFolders(entityType: EntityType): Observable<FileItem[]> {
    return this.http.get<FileItem[]>(ImagineService.getApiURL(entityType), {headers: this.getHeaders()})
      .pipe(
        tap(_ => this.logger.debug(`${this.className}.getEntityFolders: for ${entityType}`)),
        catchError(this.handleError('getEntityFiles', []))
      );
  }

  /** Returns an AWS S3 Presigned URL that grants temporary access to a protected resource */
  getPresignUrl(path: string) {
    return this.http.get<FileUrl>(path, {headers: this.getHeaders()})
      .pipe(
        tap(_ => this.logger.debug(`${this.className}.getPresignUrl: Path ${path} - success`)),
        catchError(this.handleError('getPresignUrl', []))
      );
  }

  // todo use centralized error handling
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      this.logger.error(`${operation}: ${error}`); // log to console instead
      // Let the app keep running by returning an empty result. ???
      return of(result); // as T is unnecessary since it does not change the type of expression (sonarlint)
    };
  }

  /**
   * If user is logged in and JWT is present in AuthService, make sure it is passed to
   * imagine service as Bearer token in X-Authorization HTTP Header
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
