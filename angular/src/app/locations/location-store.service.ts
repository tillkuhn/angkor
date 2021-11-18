import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityEventService} from '@shared/services/entity-event.service';
import {EntityType} from '@shared/domain/entities';
import {EntityStore} from '@shared/services/entity-store';
import {Location} from '@domain/location';
import {ApiHelper} from '@shared/helpers/api-helper';

@Injectable({
  providedIn: 'root'
})
// UI Entity, API Entity .. we use the same here
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
