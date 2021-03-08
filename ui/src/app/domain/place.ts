import {ManagedEntity} from './entities';

// Same props for API and UI Entity
interface GenericPlace extends ManagedEntity {
  id: string;
  name: string;
  areaCode: string;
  summary?: string;
  notes?: string;
  primaryUrl?: string;
  imageUrl?: string;
  tags?: string[];
  locationType?: string;
  coordinates?: number[];   // lon/länge, lat/breite
  createdBy?: string; // todo could be our own enetity with shortname
  updatedBy?: string; // todo could be our own enetity with shortname
  authScope?: string; // Todo could be tyescript enum
}

// Interface used all across the ui
export interface Place extends GenericPlace {
  createdAt?: Date; // | string
  updatedAt?: Date; // | string
  lastVisited?: Date; // todo could be enum
}

// Interface used all across the ui
export interface ApiPlace extends GenericPlace {
  createdAt?: string;
  updatedAt?: string;
  lastVisited?: string; // iso3601 for backend
}
