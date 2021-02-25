import {Injectable} from '@angular/core';
import {EntityStore, httpOptions} from '../shared/entity-store';
import {ApiDish, Dish} from '../domain/dish';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {NotificationService} from '../shared/services/notification.service';
import {EntityType} from '../domain/entities';
import {EntityHelper} from '../shared/entity-helper';
import {Observable} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import {Place} from '../domain/place';

@Injectable({
  providedIn: 'root'
})
export class DishStoreService extends EntityStore<Dish, ApiDish> {

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

  // Custom ops
  justServed(id: string): Observable<any> {
    return this.http.put<Dish>(`${this.apiUrl}/${id}/just-served`, httpOptions).pipe(
      tap((resp: any) => this.logger.debug(`just served dish result=${resp.result}`)),
      catchError(this.handleError<Place>('justServed'))
    );
  }


  // override standard mapper in superclass
  mapFromApiEntity(apiEntity: ApiDish): Dish {
    return {
      ...apiEntity,
      createdAt: EntityHelper.parseISO(apiEntity.createdAt),
      updatedAt: EntityHelper.parseISO(apiEntity.updatedAt),
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
