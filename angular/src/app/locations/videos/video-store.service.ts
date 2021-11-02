import { Injectable } from '@angular/core';
import {EntityStore} from '@shared/services/entity-store';
import {Video} from '@domain/location';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityEventService} from '@shared/services/entity-event.service';
import {EntityType} from '@shared/domain/entities';

@Injectable({
  providedIn: 'root'
})
export class VideoStoreService extends EntityStore<Video, Video> {

  constructor(http: HttpClient,
              logger: NGXLogger,
              events: EntityEventService
  ) {
    super(http, logger, events);
  }


  entityType(): EntityType {
    return EntityType.VIDEO;
  }

}
