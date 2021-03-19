import {ManagedEntity} from './entities';

export declare type LinkMediaType =  'DEFAULT' | 'VIDEO' | 'FEED' | 'AUDIO' | 'IMAGE' | 'PDF';

export interface GenericLink extends ManagedEntity {
  id: string;
  name: string;
  linkUrl: string;
  mediaType: LinkMediaType;
  entityType?: string; // todo typesafe
  authScope?: string; // Todo typesafe
  coordinates?: number[];   // lon,lat
}

export interface Link extends GenericLink {
  createdAt?: Date;
  youtubeId?: string; // frontend only, todo rename
}

export interface ApiLink extends GenericLink {
  createdAt?: string;
}
