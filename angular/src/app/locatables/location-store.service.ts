import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityEventService} from '@shared/services/entity-event.service';
import {EntityType} from '@shared/domain/entities';
import {EntityStore, httpOptions} from '@shared/services/entity-store';
import {Location} from '@domain/location';
import {ApiHelper} from '@shared/helpers/api-helper';
import {Observable} from 'rxjs';
import {tap} from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
// UI Entity, API Entity ... we use the same here
export class LocationStoreService extends EntityStore<Location, Location> {

  constructor(http: HttpClient,
              logger: NGXLogger,
              events: EntityEventService
  ) {
    super(http, logger, events);
  }

  // list of tags that may be suggested as tags for this entity

  entityType(): EntityType {
    return EntityType.Location;
  }

  /**
   * Create a new item
   * @param importUrl external URL to import from
   * @param targetEntityType targetEntityType
   * @return newly imported item from API
   */
  importLocation(importUrl: string, targetEntityType: EntityType): Observable<any> {
    const operation = `${this.className}.import${targetEntityType}`;
    // const apiItem = this.mapToApiEntity(item);
    const apiUrl = ApiHelper.getApiUrl(targetEntityType) + '/import';
    this.logger.info(`${operation} Request import @ ${apiUrl}`)
    return this.http.post<any>(apiUrl, {targetEntityType, importUrl}, httpOptions).pipe(
      // map<AE, E>(updatedApiItem => this.mapFromApiEntity(updatedApiItem)),
      tap(addedItem => this.events.emit({action: 'CREATE', entityType: targetEntityType, entity: addedItem})),
      // catchError(ApiHelper.handleError<any>(operation, this.events)) // what to return instead of any??
    );
  }

  // override standard mapper in superclass
  mapFromApiEntity(apiEntity: Location): Location {
    return {
      ...apiEntity,
      createdAt: ApiHelper.parseISO(apiEntity.createdAt),
      updatedAt: ApiHelper.parseISO(apiEntity.updatedAt),
      // beenThere: ApiHelper.parseISO(apiEntity.beenThere),
    };
  }

  // override standard mapper in superclass
  protected mapToApiEntity(uiEntity: Location): Location {
    // https://ultimatecourses.com/blog/remove-object-properties-destructuring
    const {
      createdAt, // remove
      updatedAt, // remove
      // beenThere, // remove
      ...rest  // ... what remains
    } = uiEntity;
    return {
      ...rest
      //  beenThere: ApiHelper.formatISO(uiEntity.beenThere) // api
    };
  }
}
