/*
 * Location types also used in
 * dropdown for places
 */

import {Moment} from 'moment';
import {ListItem} from './list-item';

export interface Place {
  id: string;
  name: string;
  areaCode: string;
  summary?: string;
  notes?: string;
  primaryUrl?: string;
  imageUrl?: string;
  locationType?: string;
  coordinates?: number[];   // lon/länge, lat/breite
  createdAt?: Moment;
  updatedAt?: Moment;
  authScope?: string | ListItem; // Todo typesafe
}

// https://basarat.gitbook.io/typescript/type-system/index-signatures#typescript-index-signature
export interface LocationType {
  label: string;
  icon: string;
  value: string;
}
