import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EntityEventService} from '@shared/services/entity-event.service';
import {NGXLogger} from 'ngx-logger';
import {Observable} from 'rxjs';
import {Metric} from '@app/admin/metrics/metric';
import {environment} from '../../environments/environment';
import {catchError, tap} from 'rxjs/operators';
import {ApiHelper} from '@shared/helpers/api-helper';

@Injectable({
  providedIn: 'root'
})
export class AdminService {

  private readonly className = 'AdminService';

  constructor(private http: HttpClient,
              private events: EntityEventService,
              private logger: NGXLogger) {
  }

  triggerAction(action: string): Observable<any> {
    return this.http.post<any>(`${environment.apiUrlRoot}/admin/actions/${action}`, {})
      .pipe(
        tap(metrics => this.logger.debug(`${this.className}: fetched ${metrics.length} metrics`)),
        catchError(ApiHelper.handleError('getMetrics', this.events, []))
      );
  }

  // for some reason we can't use Observable<Map<string, string>> here :-(
  actions(): Observable<any> {
    return this.http.get<Map<string, string>>(`${environment.apiUrlRoot}/admin/actions`)
      .pipe(
        tap(actions => this.logger.debug(`${this.className}: fetched ${actions} actions`)),
        catchError(ApiHelper.handleError('actions', this.events, []))
      );
  }

  getMetrics(): Observable<Metric[]> {
    return this.http.get<Metric[]>(`${environment.apiUrlRoot}/admin/metrics`)
      .pipe(
        tap(metrics => this.logger.debug(`${this.className}: fetched ${metrics.length} metrics`)),
        catchError(ApiHelper.handleError('getMetrics', this.events, []))
      );
  }

  getStats(): Observable<any> {
    return this.http.get<any>(`${environment.apiUrlRoot}/stats`)
      .pipe(
        tap(_ => this.logger.debug(`ApiService.getStats: fetched stats`)),
        catchError(ApiHelper.handleError('getStats', this.events, {}))
      );
  }

}
