import {ManagedEntity} from '@shared/domain/entities';
import {POI} from '@domain/poi';

export declare type LinkMediaType =  'DEFAULT' | 'VIDEO' | 'FEED' | 'AUDIO' | 'IMAGE' | 'PDF';

export interface GenericTour extends ManagedEntity {
  id?: string; // first known *after* entity is created
  name: string;
  primaryUrl: string;
  authScope?: string; // Todo typesafe
  coordinates?: number[];   // lon,lat
}

// For us (UI)
export interface Tour extends GenericTour, POI {
  createdAt?: Date;
}

// For Backend
export interface ApiTour extends GenericTour {
  createdAt?: string;
}
