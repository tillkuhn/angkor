import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {HttpClient, HttpErrorResponse} from '@angular/common/http';
import {catchError, map, tap} from 'rxjs/operators';
import {Place} from '../../domain/place';
import {environment} from '../../../environments/environment';
import {NGXLogger} from 'ngx-logger';
import {Area} from '../../domain/area';
import {POI} from '../../domain/poi';
import {Dish} from '../../domain/dish';
import {Note} from '../../domain/note';
import {Metric} from '../../admin/metrics/metric';
import {AreaNode} from '../../domain/area-node';
import {EntityType} from '../../domain/entities';
import {format, parseISO} from 'date-fns';
import {MatSnackBar} from '@angular/material/snack-bar';
import {httpOptions} from '../entity-store';
import {EntityHelper} from '../entity-helper';


@Injectable({
  providedIn: 'root'
})
export class ApiService {

  readonly apiUrlAreas = EntityHelper.getApiUrl(EntityType.Area);

  constructor(private http: HttpClient,
              private snackBar: MatSnackBar,
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
        catchError(this.handleError('getCountries', []))
      );
  }

  getAreaTree(): Observable<AreaNode[]> {
    return this.http.get<AreaNode[]>(environment.apiUrlRoot + '/area-tree')
      .pipe(
        // tap: Perform a side effect for every emission on the source Observable, but return an Observable that is identical to the source.
        tap(_ => this.logger.debug('ApiService fetched getAreaTree')),
        catchError(this.handleError('getAreaTree', []))
      );
  }

  addArea(area: Area): Observable<Area> {
    return this.http.post<Area>(this.apiUrlAreas, area, httpOptions).pipe(
      tap((prod: any) => this.logger.debug(`added area w/ id=${prod.id}`)),
      catchError(this.handleError<Place>('addArea()'))
    );
  }


  getPOIs(): Observable<POI[]> {
    return this.http.get<POI[]>(environment.apiUrlRoot + '/pois')
      .pipe(
        tap(pois => this.logger.debug(`ApiService.getPOIs fetched ${pois.length} pois`)),
        catchError(this.handleError('getPOIs', []))
      );
  }

  getMetrics(): Observable<Metric[]> {
    return this.http.get<Metric[]>(`${environment.apiUrlRoot}/admin/metrics`)
      .pipe(
        tap(metrics => this.logger.debug(`svc fetched ${metrics.length} metrics`)),
        catchError(this.handleError('getDishes', []))
      );
  }

  /**
   * Generic Error Handler
   */
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // IMPROVEMENT: send the error to remote logging infrastructure
      if (error instanceof HttpErrorResponse) {
        const e = error as HttpErrorResponse;
        this.logger.info('message:', e.message, 'status:', e.status);
        if (e.status === 403) {
          this.snackBar.open('Access to item is forbidden, check if you are authenticated!',
            'Acknowledge', {duration: 5000});
          // maybe also reroute: https://stackoverflow.com/a/56971256/4292075
          // .onAction()
          //   .subscribe(() => this.router.navigateByUrl('/app/user/detail'));
        }
      }
      this.logger.error('error during operation', operation, error); // log to console instead

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

}
