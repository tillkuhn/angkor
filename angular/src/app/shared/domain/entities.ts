// export declare type EntityType = 'Place' | 'Note' | 'Dish';
// export declare type EntityTypePath = 'places' | 'notes' | 'dishes';

/**
 * EntityType Enum.
 * TODO make all enum names be uppercase to be consistent with backend
 * , ... or PascalCase? Reason: Similar to Class
 *  https://www.typescriptlang.org/docs/handbook/enums.html#string-enums
 */
export enum EntityType {

  Place = 'Place',
  Dish = 'Dish',
  Note = 'Note',
  Area = 'Area',
  User = 'User',
  Tag = 'Tag',
  TOUR = 'TOUR',
  LINK = 'LINK',
  LOCATION = 'LOCATION'
  // User = 'USER'
}

export interface ManagedEntity {
  id?: string; // first known *after* entity is created
}
