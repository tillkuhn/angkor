/*
 * Location types also used in
 * dropdown for places
 */

import {Moment} from 'moment';

export interface Place {
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
  createdAt?: Moment;
  updatedAt?: Moment;
  authScope?: string; // Todo typesafe
}
