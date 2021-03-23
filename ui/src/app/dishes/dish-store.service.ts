import {Injectable} from '@angular/core';
import {EntityStore, httpOptions} from '@shared/services/entity-store';
import {ApiDish, Dish} from '@domain/dish';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType} from '@domain/entities';
import {ApiHelper} from '@shared/helpers/api-helper';
import {Observable} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import {Place} from '@domain/place';
import {EntityEventService} from '@shared/services/entity-event.service';

@Injectable({
  providedIn: 'root'
})
export class DishStoreService extends EntityStore<Dish, ApiDish> {

  constructor(http: HttpClient,
              logger: NGXLogger,
              events: EntityEventService,
  ) {
    super(http, logger, events);
  }

  // must override
  entityType(): EntityType {
    return EntityType.Dish;
  }

  // Custom ops
  justServed(id: string): Observable<any> {
    return this.http.put<Dish>(`${this.apiUrl}/${id}/just-served`, httpOptions).pipe(
      tap((resp: any) => this.logger.debug(`just served dish result=${resp.result}`)),
      catchError(ApiHelper.handleError<Place>('justServed', this.events))
    );
  }


  // override standard mapper in superclass
  mapFromApiEntity(apiEntity: ApiDish): Dish {
    return {
      ...apiEntity,
      createdAt: ApiHelper.parseISO(apiEntity.createdAt),
      updatedAt: ApiHelper.parseISO(apiEntity.updatedAt),
    };
  }

  // override standard mapper in superclass
  protected mapToApiEntity(uiEntity: Dish): ApiDish {
    // https://ultimatecourses.com/blog/remove-object-properties-destructuring
    const {
      createdAt, // remove
      updatedAt, // remove
      ...rest  // ... what remains
    } = uiEntity;
    return {
      ...rest
    };
  }
}
