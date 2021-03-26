import {Injectable} from '@angular/core';
import {EntityStore, httpOptions} from '@shared/services/entity-store';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType} from '@shared/domain/entities';
import {Area, AreaNode, GenericArea} from '@domain/area';
import {Observable} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import {ApiHelper} from '@shared/helpers/api-helper';
import {POI} from '@domain/poi';
import {environment} from '../../environments/environment';
import {EntityEventService} from '@shared/services/entity-event.service';

@Injectable({
  providedIn: 'root'
})
export class AreaStoreService extends EntityStore<Area, GenericArea> {

  constructor(http: HttpClient,
              logger: NGXLogger,
              events: EntityEventService,
  ) {
    super(http, logger, events);
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
        catchError(ApiHelper.handleError('getPOIs', this.events, []))
      );
  }

  getAreaTree(): Observable<AreaNode[]> {
    return this.http.get<AreaNode[]>(this.apiUrl + '/tree/')
      .pipe(
        // tap: Perform a side effect for every emission on the source Observable, but return an Observable that is identical to the source.
        tap(_ => this.logger.debug('ApiService fetched getAreaTree')),
        catchError(ApiHelper.handleError('getAreaTree', this.events, []))
      );
  }

  addArea(area: Area): Observable<Area> {
    return this.http.post<Area>(this.apiUrl, area, httpOptions).pipe(
      tap((prod: any) => this.logger.debug(`added area w/ id=${prod.id}`)),
      catchError(ApiHelper.handleError<any>('addArea()', this.events, {}))
    );
  }

  // override standard mapper in superclass
  // map id to code so we can used normalized with ManagedEntiy interface for areas
  mapFromApiEntity(apiEntity: GenericArea): Area {
    return {
      ...apiEntity,
      id: apiEntity.code
    };
  }

  // override standard mapper in superclass, remove id
  protected mapToApiEntity(uiEntity: Area): GenericArea {
    const {
      id, // remove
      ...rest  // ... what remains
    } = uiEntity;
    return {
      ...rest,
    };
  }

}
