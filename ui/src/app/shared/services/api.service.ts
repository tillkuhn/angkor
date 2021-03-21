import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {catchError, tap} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import {NGXLogger} from 'ngx-logger';
import {Metric} from '../../admin/metrics/metric';
import {ApiHelper} from '../helpers/api-helper';
import {NotificationService} from './notification.service';


@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private http: HttpClient,
              private notifier: NotificationService,
              private logger: NGXLogger) {
  }

  getMetrics(): Observable<Metric[]> {
    return this.http.get<Metric[]>(`${environment.apiUrlRoot}/admin/metrics`)
      .pipe(
        tap(metrics => this.logger.debug(`svc fetched ${metrics.length} metrics`)),
        catchError(ApiHelper.handleError('getMetrics', this.notifier, []))
      );
  }

  getStats(): Observable<any> {
    return this.http.get<any>(`${environment.apiUrlRoot}/stats`)
      .pipe(
        tap(metrics => this.logger.debug(`ApiService.getStats: fetched stats`)),
        catchError(ApiHelper.handleError('getStats', this.notifier, {}))
      );
  }


}
