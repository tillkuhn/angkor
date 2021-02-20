import { Injectable } from '@angular/core';
import {EntityStore} from '../entity-store';
import {ApiDish, Dish} from '../domain/dish';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {NotificationService} from '../shared/services/notification.service';
import {EntityType} from '../domain/entities';
import {ApiPlace, Place} from '../domain/place';
import {EntityHelper} from '../entity-helper';

@Injectable({
  providedIn: 'root'
})
export class DishStoreService extends EntityStore<Dish, ApiDish>{

  constructor(http: HttpClient,
              logger: NGXLogger,
              notifier: NotificationService,
  ) {
    super(http, logger, notifier);
  }

  // must override
  entityType(): EntityType {
    return EntityType.Dish;
  }

  // override standard mapper in superclass
  mapFromApiEntity(apiEntity: ApiDish): Dish {
    return {
      ...apiEntity,
      createdAt: EntityHelper.parseDate(apiEntity.createdAt),
      updatedAt: EntityHelper.parseDate(apiEntity.updatedAt),
    };
  }

}
