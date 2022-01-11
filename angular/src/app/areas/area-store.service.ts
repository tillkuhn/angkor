import {ApiHelper} from '@shared/helpers/api-helper';
import {Area, AreaNode, GenericArea} from '@domain/area';
import {EntityEventService} from '@shared/services/entity-event.service';
import {EntityStore, httpOptions} from '@shared/services/entity-store';
import {EntityType} from '@shared/domain/entities';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {NGXLogger} from 'ngx-logger';
import {Observable} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';

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
  // map id to code, so we can be used normalized with ManagedEntity interface for areas
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
