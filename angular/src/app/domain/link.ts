import {ManagedEntity} from '@shared/domain/entities';
import {POI} from '@domain/poi';

export declare type LinkMediaType =  'DEFAULT' | 'VIDEO' | 'FEED' | 'AUDIO' | 'IMAGE' | 'PDF';

export interface GenericLink extends ManagedEntity {
  id?: string; // first known *after* entity is created
  name: string;
  linkUrl: string;
  mediaType: LinkMediaType;
  entityType?: string; // todo typesafe
  authScope?: string; // Todo typesafe
  coordinates?: number[];   // lon,lat
}

// For us (UI)
export interface Link extends GenericLink, POI {
  createdAt?: Date;
  youtubeId?: string; // frontend only, todo rename
}

// For Backend
export interface ApiLink extends GenericLink {
  createdAt?: string;
}
