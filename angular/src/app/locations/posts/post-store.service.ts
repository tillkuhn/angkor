import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityEventService} from '@shared/services/entity-event.service';
import {EntityType} from '@shared/domain/entities';
import {EntityStore} from '@shared/services/entity-store';
import {Post} from '@domain/location';

@Injectable({
  providedIn: 'root'
})
export class PostStoreService extends EntityStore<Post, Post>{

  constructor(http: HttpClient,
              logger: NGXLogger,
              events: EntityEventService
  ) {
    super(http, logger, events);
  }


  entityType(): EntityType {
    return EntityType.POST;
  }

}
