import {Injectable} from '@angular/core';
import {EntityStore} from '../entity-store';
import {ApiNote, Note} from '../domain/note';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {NotificationService} from '../shared/services/notification.service';
import {EntityType} from '../domain/entities';
import {EntityHelper} from '../entity-helper';
import {format} from 'date-fns';

@Injectable({
  providedIn: 'root'
})
export class NoteStoreService extends EntityStore<Note, ApiNote> {

  constructor(http: HttpClient,
              logger: NGXLogger,
              notifier: NotificationService,
  ) {
    super(http, logger, notifier);
  }

  entityType(): EntityType {
    return EntityType.Note;
  }

  // override standard mapper in superclass
  mapFromApiEntity(apiEntity: ApiNote): Note {
    return {
      ...apiEntity,
      createdAt: EntityHelper.parseISO(apiEntity.createdAt),
      dueDate: EntityHelper.parseISO(apiEntity.dueDate)
    };
  }

  // override standard mapper in superclass
  protected mapToApiEntity(uiEntity: Note): ApiNote {
    // https://ultimatecourses.com/blog/remove-object-properties-destructuring
    const {
      createdAt, // remove
      ...rest  // ... what remains
    } = uiEntity;
    return {
      ...rest,
      dueDate: uiEntity.dueDate ? format(uiEntity.dueDate as Date, 'yyyy-MM-dd') : null  // EntityHelper.formatISO(uiEntity.dueDate) // api
    };
  }

}
