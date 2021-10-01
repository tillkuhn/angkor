import {ApiHelper} from '@shared/helpers/api-helper';
import {ApiPlace, Place} from '@domain/place';
import {EntityEventService} from '@shared/services/entity-event.service';
import {EntityStore} from '@shared/services/entity-store';
import {EntityType} from '@shared/domain/entities';
import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {NGXLogger} from 'ngx-logger';

@Injectable({
  providedIn: 'root'
})
export class PlaceStoreService extends EntityStore<Place, ApiPlace> {

  constructor(http: HttpClient,
              logger: NGXLogger,
              events: EntityEventService,
  ) {
    super(http, logger, events);
  }

  // must override
  entityType(): EntityType {
    return EntityType.Place;
  }

  // override standard mapper in superclass
  mapFromApiEntity(apiEntity: ApiPlace): Place {
    return {
      ...apiEntity,
      createdAt: ApiHelper.parseISO(apiEntity.createdAt),
      updatedAt: ApiHelper.parseISO(apiEntity.updatedAt),
      lastVisited: ApiHelper.parseISO(apiEntity.lastVisited)
    };
  }

  // override standard mapper in superclass
  protected mapToApiEntity(uiEntity: Place): ApiPlace {
    // https://ultimatecourses.com/blog/remove-object-properties-destructuring
    const {
      createdAt, // remove
      createdBy, // remove
      updatedAt, // remove
      updatedBy, // remove
      ...rest  // ... what remains
    } = uiEntity;
    return {
      ...rest,
      lastVisited: ApiHelper.formatISO(uiEntity.lastVisited) // api
    };
  }

}
