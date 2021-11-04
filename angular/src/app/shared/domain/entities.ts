import {TitleCasePipe} from '@angular/common';
// export declare type EntityType = 'Place' | 'Note' | 'Dish';
// export declare type EntityTypePath = 'places' | 'notes' | 'dishes';


export interface ManagedEntity {
  id?: string; // first known *after* entity is created
}

/**
 * EntityType Enum.
 * TODO make all enum names be uppercase to be consistent with backend
 * , ... or PascalCase? Reason: Similar to Class
 *  https://www.typescriptlang.org/docs/handbook/enums.html#string-enums
 */

export enum EntityType {
  // pls maintain alphabetical order
  Area = 'Area',
  Dish = 'Dish',
  LINK = 'LINK',
  LOCATION = 'LOCATION',
  Note = 'Note',
  Place = 'Place',
  TOUR = 'TOUR',
  Tag = 'Tag',
  User = 'USER',
  VIDEO = 'VIDEO',
}


/**
 * MetaData for enums
 * Should also replace static ApiHelper.getApiPath(entityType: EntityType) {
 */
export class EntityMetadata {
  id: EntityType;
  path: string;
  name: string;
  icon: string;
  iconUrl: string;
  constructor(entityType: EntityType) {
    this.id = entityType;
    this.name = new TitleCasePipe().transform(entityType.toLowerCase());
    this.path = entityType.toLowerCase() + 's';
    this.icon = entityType.toLowerCase();
    this.iconUrl = `/assets/icons/${entityType.toLowerCase()}.svg`;
  }
}

/**
 * Lookup EntityMetadata by key (can take String or Enum)
 */
export const EntityMetadataLookup: {[key: string]: EntityMetadata} = {};

for (const enumKey of Object.keys(EntityType)) { // values doesn't allow lookup
  const id = EntityType[enumKey]; // translates to the string VALUE of the enum
  EntityMetadataLookup[id] = new EntityMetadata(id);
}
// Debug enum
// https://stackoverflow.com/a/40055555/4292075
// Object.entries(EntityType).forEach(
//  ([key, value]) => console.log('entry',key, 'value', value)
// );
