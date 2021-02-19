import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {EntityType} from '../domain/entities';
import {Place} from '../domain/place';
import {ListItem} from '../domain/list-item';
import {EntityStore} from '../entity-store';
import {EntityHelper} from '../entity-helper';

@Injectable({
  providedIn: 'root'
})
export class PlaceStoreService extends EntityStore<Place, Place> {

  sortProperties: ListItem[] = [
    {value: 'name', label: 'Name'},
    {value: 'areaCode', label: 'Region'},
    {value: 'locationType', label: 'Type'},
    {value: 'updatedAt', label: 'Updated'},
    {value: 'authScope', label: 'Authscope'}
  ];

  constructor(http: HttpClient,
              logger: NGXLogger
  ) {
    super(http, logger);
  }

  // must override
  entityType(): EntityType {
    return EntityType.Place;
  }

  // reverseSortOrder() {
  //   const currentOrder = this.searchRequest.sortDirection;
  //   this.searchRequest.sortDirection = currentOrder === 'ASC' ? 'DESC' : 'ASC';
  // }

  // todo support multiple, workaround to bind select box to first array element
  get sortProperty() {
    return this.searchRequest.sortProperties[0];
  }

  set sortProperty(sortProperty) {
    this.searchRequest.sortProperties[0] = sortProperty;
  }

  // override standard mapper in superclass
  mapFromApiEntity(apiEntity: Place): Place {
    return {
      ...apiEntity,
      createdAt: EntityHelper.parseDate(apiEntity.createdAt),
      updatedAt: EntityHelper.parseDate(apiEntity.updatedAt),
      lastVisited: EntityHelper.parseDate(apiEntity.lastVisited)
    };
  }

}
