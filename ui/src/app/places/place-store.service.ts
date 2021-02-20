import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType} from '../domain/entities';
import {ApiPlace, Place} from '../domain/place';
import {EntityStore} from '../entity-store';
import {EntityHelper} from '../entity-helper';
import {MatSnackBar} from '@angular/material/snack-bar';
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
      createdAt: EntityHelper.parseDate(apiEntity.createdAt),
      updatedAt: EntityHelper.parseDate(apiEntity.updatedAt),
      lastVisited: EntityHelper.parseDate(apiEntity.lastVisited)
    };
  }

}
