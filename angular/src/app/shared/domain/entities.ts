import {TitleCasePipe} from '@angular/common';
// export declare type EntityType = 'Place' | 'Note' | 'Dish';
// export declare type EntityTypePath = 'places' | 'notes' | 'dishes';


export interface ManagedEntity {
  id?: string; // first known *after* entity is created
}

/**
 * EntityType Enum, should be consistent with Backend Enum Values
 *
 * Use PascalCase Reason similar to Class Names
 * Ref: https://www.typescriptlang.org/docs/handbook/enums.html#string-enums
 */

export enum EntityType {
  // pls maintain alphabetical order
  Area = 'Area',
  Dish = 'Dish',
  Link = 'Link',
  Location = 'Location',
  Note = 'Note',
  Place = 'Place',
  Photo = 'Photo',
  Post = 'Post',
  Tag = 'Tag',
  Tour = 'Tour',
  User = 'User',
  Video = 'Video',
}


/**
 * MetaData for enums
 * Should also replace static ApiHelper.getApiPath(entityType: EntityType) {
 */
export class EntityTypeInfo {
  id: EntityType;
  path: string;
  name: string;
  namePlural: string;
  icon: string;
  iconUrl: string;
  title: string;

  constructor(entityType: EntityType) {
    this.id = entityType;
    this.name = new TitleCasePipe().transform(entityType.toLowerCase());
    this.namePlural = `${this.name}s`;
    this.path = `${entityType.toLowerCase()}s`;
    this.icon = entityType.toLowerCase();
    this.iconUrl = `/assets/icons/${this.icon}.svg`;
    this.title = this.name;
  }
}

/**
 * Lookup EntityMetadata by key (can take String or Enum)
 *
 * Examples:
 *   let vInfo: EntityTypeInfo = EntityMetadata[EntityType.VIDEO];
 *   let tInfo: EntityTypeInfo = EntityMetadata['TOUR'];
 *
 */
// export const EntityMetadata: {[key: string]: EntityTypeInfo} = {};
//  *
// since Typescript 2.1 there is a built in type Record<T, K> that acts like a dictionary.
// You could also limit/specify potential keys using union literal types:
// var stuff: Record<'a'|'b'|'c', string|boolean> = {};
// https://stackoverflow.com/a/51161730/4292075
export const EntityMetadata: Record<string, EntityTypeInfo> = {};

// Fill with default values
for (const enumKey of Object.keys(EntityType)) { // values doesn't allow lookup
  const id = EntityType[enumKey]; // translates to the string VALUE of the enum
  EntityMetadata[id] = new EntityTypeInfo(id);
}

// Overwrites for special cases ....
EntityMetadata[EntityType.Dish].path = 'dishes';
EntityMetadata[EntityType.Dish].namePlural = 'Dishes';

EntityMetadata[EntityType.Dish].title = 'Wish a Dish';
EntityMetadata[EntityType.Note].title = 'MerksteWas?';
EntityMetadata[EntityType.Place].title = 'Places2Go';
EntityMetadata[EntityType.Photo].title = 'PhotoGallery';
EntityMetadata[EntityType.Post].title = 'BlogParty';
EntityMetadata[EntityType.Tour].title = 'TourGuide';
EntityMetadata[EntityType.Video].title = 'Schaumerma!';


// Debug enum
// https://stackoverflow.com/a/40055555/4292075
// Object.entries(EntityType).forEach(
//  ([key, value]) => console.log('entry',key, 'value', value)
// );
