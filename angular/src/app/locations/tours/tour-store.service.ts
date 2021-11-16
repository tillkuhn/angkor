import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityEventService} from '@shared/services/entity-event.service';
import {EntityType} from '@shared/domain/entities';
import {EntityStore} from '@shared/services/entity-store';
import {Tour} from '@domain/location';
import {ApiHelper} from '@shared/helpers/api-helper';

@Injectable({
  providedIn: 'root'
})
// for new Location Types: UI Entity == API Entity
export class TourStoreService extends EntityStore<Tour, Tour> {

  constructor(http: HttpClient,
              logger: NGXLogger,
              events: EntityEventService
  ) {
    super(http, logger, events);
  }

  // list of tags that may be suggested as tags for this entity
  entityType(): EntityType {
    return EntityType.Tour;
  }

  // override standard mapper in superclass
  mapFromApiEntity(apiEntity: Tour): Tour {
    return {
      ...apiEntity,
      createdAt: ApiHelper.parseISO(apiEntity.createdAt),
      updatedAt: ApiHelper.parseISO(apiEntity.updatedAt),
      beenThere: ApiHelper.parseISO(apiEntity.beenThere),
    };
  }

  // override standard mapper in superclass
  protected mapToApiEntity(uiEntity: Tour): Tour {
    // https://ultimatecourses.com/blog/remove-object-properties-destructuring
    const {
      createdAt, // remove
      updatedAt, // remove
      beenThere, // remove
      ...rest  // ... what remains
    } = uiEntity;
    return {
      ...rest
      //  lastVisited: ApiHelper.formatISO(uiEntity.lastVisited) // api
    };
  }
}
