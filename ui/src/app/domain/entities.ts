// export declare type EntityType = 'Place' | 'Note' | 'Dish';
// export declare type EntityTypePath = 'places' | 'notes' | 'dishes';

/**
 * .. Enum. Use PascalCase for enum names. Reason: Similar to Class
 * See https://www.typescriptlang.org/docs/handbook/enums.html#string-enums
 */
export enum EntityType {
  Place = 'Place',
  Dish = 'Dish',
  Note = 'Note',
  Area = 'Area',
  User = 'User',
  Tag = 'Tag',
  LINK = 'LINK' // TODO all should be uppercase
  // User = 'USER'
}

export interface ManagedEntity {
  id?: string;
}
