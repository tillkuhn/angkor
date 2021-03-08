import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType} from '../domain/entities';
import {ApiPlace, Place} from '../domain/place';
import {EntityStore} from '../shared/services/entity-store';
import {ApiHelper} from '../shared/helpers/api-helper';
import {NotificationService} from '../shared/services/notification.service';

@Injectable({
  providedIn: 'root'
})
export class PlaceStoreService extends EntityStore<Place, ApiPlace> {

  constructor(http: HttpClient,
              logger: NGXLogger,
              notifier: NotificationService,
  ) {
    super(http, logger, notifier);
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
