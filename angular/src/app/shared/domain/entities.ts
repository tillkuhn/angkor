// export declare type EntityType = 'Place' | 'Note' | 'Dish';
// export declare type EntityTypePath = 'places' | 'notes' | 'dishes';

/**
 * EntityType Enum.
 * TODO make all enum names be uppercase to be consistent with backend
 * , ... or PascalCase? Reason: Similar to Class
 *  https://www.typescriptlang.org/docs/handbook/enums.html#string-enums
 */
export enum EntityType {
  Area = 'Area',
  Dish = 'Dish',
  LINK = 'LINK',
  LOCATION = 'LOCATION',
  Note = 'Note',
  Place = 'Place',
  TOUR = 'TOUR',
  Tag = 'Tag',
  User = 'User',
  VIDEO = 'VIDEO',
}

export interface ManagedEntity {
  id?: string; // first known *after* entity is created
}
