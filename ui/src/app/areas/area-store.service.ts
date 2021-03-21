import { Injectable } from '@angular/core';
import {EntityStore, httpOptions} from '../shared/services/entity-store';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {NotificationService} from '../shared/services/notification.service';
import {EntityType} from '../domain/entities';
import {Area} from '../domain/area';
import {Observable} from 'rxjs';
import {AreaNode} from '../domain/area-node';
import {catchError, tap} from 'rxjs/operators';
import {ApiHelper} from '../shared/helpers/api-helper';
import {POI} from '../domain/poi';
import {environment} from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AreaStoreService extends EntityStore<Area, Area> {

  constructor(http: HttpClient,
              logger: NGXLogger,
              notifier: NotificationService,
  ) {
    super(http, logger, notifier);
  }

  // must override
  entityType(): EntityType {
    return EntityType.Area;
  }

  // Check if we find a new place ...
  getPOIs(): Observable<POI[]> {
    return this.http.get<POI[]>(environment.apiUrlRoot + '/pois')
      .pipe(
        tap(pois => this.logger.debug(`ApiService.getPOIs fetched ${pois.length} pois`)),
        catchError(ApiHelper.handleError('getPOIs', this.notifier, []))
      );
  }

  getAreaTree(): Observable<AreaNode[]> {
    return this.http.get<AreaNode[]>(this.apiUrl + '/tree/')
      .pipe(
        // tap: Perform a side effect for every emission on the source Observable, but return an Observable that is identical to the source.
        tap(_ => this.logger.debug('ApiService fetched getAreaTree')),
        catchError(ApiHelper.handleError('getAreaTree', this.notifier, []))
      );
  }

  addArea(area: Area): Observable<Area> {
    return this.http.post<Area>(this.apiUrl, area, httpOptions).pipe(
      tap((prod: any) => this.logger.debug(`added area w/ id=${prod.id}`)),
      catchError(ApiHelper.handleError<any>('addArea()', this.notifier, {}))
    );
  }

}
