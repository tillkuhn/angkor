import {Injectable} from '@angular/core';
import {EntityStore, httpOptions} from '@shared/services/entity-store';
import {ApiNote, Note} from '@domain/note';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType} from '@shared/domain/entities';
import {ApiHelper} from '@shared/helpers/api-helper';
import {EntityEventService} from '@shared/services/entity-event.service';
import {catchError, map, tap} from 'rxjs/operators';
import {Observable} from 'rxjs';
import {ApiPlace} from '@domain/place';

@Injectable({
  providedIn: 'root'
})
export class NoteStoreService extends EntityStore<Note, ApiNote> {

  constructor(http: HttpClient,
              logger: NGXLogger,
              events: EntityEventService
  ) {
    super(http, logger, events);
  }

  // list of tags that may be suggested as tags for this entity

  entityType(): EntityType {
    return EntityType.Note;
  }

  // convert Note to a new Place
  convertToPlace(uiEntity: Note): Observable<string> {
    const operation = `${this.className}.convert{this.entityType()}ToPlace`;
    const apiItem = this.mapToApiEntity(uiEntity);
    return this.http.post<ApiPlace>(this.apiUrl + '/to-place', apiItem, httpOptions).pipe(
      map<ApiPlace, string>(createdPlace => createdPlace.id),
      tap(addedItem => this.events.emit( {action: 'CREATE', entityType: EntityType.Place, entity: {id: addedItem} }) ),
      catchError(ApiHelper.handleError<any>(operation, this.events)) // what to return instead of any??
    );
  }

  // override standard mapper in superclass
  mapFromApiEntity(apiEntity: ApiNote): Note {
    return {
      ...apiEntity,
      createdAt: ApiHelper.parseISO(apiEntity.createdAt),
      dueDate: ApiHelper.parseISO(apiEntity.dueDate)
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
      dueDate: ApiHelper.formatISOasShortDate(uiEntity.dueDate) // 'yyyy-MM-dd' which can be parsed into LocalDate by backend
    };
  }

}
