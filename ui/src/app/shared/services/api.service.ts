import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {HttpClient} from '@angular/common/http';
import {catchError, tap} from 'rxjs/operators';
import {environment} from '../../../environments/environment';
import {NGXLogger} from 'ngx-logger';
import {Area} from '../../domain/area';
import {POI} from '../../domain/poi';
import {Metric} from '../../admin/metrics/metric';
import {AreaNode} from '../../domain/area-node';
import {EntityType} from '../../domain/entities';
import {httpOptions} from './entity-store';
import {ApiHelper} from '../helpers/api-helper';
import {NotificationService} from './notification.service';


@Injectable({
  providedIn: 'root'
})
export class ApiService {

  readonly apiUrlAreas = ApiHelper.getApiUrl(EntityType.Area);

  constructor(private http: HttpClient,
              private notifier: NotificationService,
              private logger: NGXLogger) {
  }

  /**
   * Area codes, countries, PoIs  and regions
   */
  getCountries(): Observable<Area[]> {
    return this.http.get<Area[]>(environment.apiUrlRoot + '/countries')
      .pipe(
        // tap: Perform a side effect for every emission on the source Observable, but return an Observable that is identical to the source.
        tap(_ => this.logger.debug('getCountries fetched  countries')),
        catchError(ApiHelper.handleError('getCountries', this.notifier, []))
      );
  }

  getAreaTree(): Observable<AreaNode[]> {
    return this.http.get<AreaNode[]>(environment.apiUrlRoot + '/area-tree')
      .pipe(
        // tap: Perform a side effect for every emission on the source Observable, but return an Observable that is identical to the source.
        tap(_ => this.logger.debug('ApiService fetched getAreaTree')),
        catchError(ApiHelper.handleError('getAreaTree', this.notifier, []))
      );
  }

  addArea(area: Area): Observable<Area> {
    return this.http.post<Area>(this.apiUrlAreas, area, httpOptions).pipe(
      tap((prod: any) => this.logger.debug(`added area w/ id=${prod.id}`)),
      catchError(ApiHelper.handleError<any>('addArea()', this.notifier, {}))
    );
  }

  getPOIs(): Observable<POI[]> {
    return this.http.get<POI[]>(environment.apiUrlRoot + '/pois')
      .pipe(
        tap(pois => this.logger.debug(`ApiService.getPOIs fetched ${pois.length} pois`)),
        catchError(ApiHelper.handleError('getPOIs', this.notifier, []))
      );
  }

  getMetrics(): Observable<Metric[]> {
    return this.http.get<Metric[]>(`${environment.apiUrlRoot}/admin/metrics`)
      .pipe(
        tap(metrics => this.logger.debug(`svc fetched ${metrics.length} metrics`)),
        catchError(ApiHelper.handleError('getDishes', this.notifier, []))
      );
  }

  getStats(): Observable<any> {
    return this.http.get<any>(`${environment.apiUrlRoot}/stats`)
      .pipe(
        tap(metrics => this.logger.debug(`svc fetched stats`)),
        catchError(ApiHelper.handleError('getStats', this.notifier, {}))
      );
  }


}
