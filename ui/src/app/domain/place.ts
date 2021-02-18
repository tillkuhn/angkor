/*
 * Location types also used in
 * dropdown for places
 */

import {ManagedEntity} from './entities';

export interface Place extends ManagedEntity{
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
  createdAt?: Date | string;
  createdBy?: string;
  updatedAt?: Date | string;
  updatedBy?: string;
  lastVisited?: Date | string; // Todo use different date object
  authScope?: string; // Todo typesafe
}
