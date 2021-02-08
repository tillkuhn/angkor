/*
 * Location types also used in
 * dropdown for places
 */

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
  createdAt?: Date | string;
  createdBy?: string;
  updatedAt?: Date | string;
  updatedBy?: string;
  lastVisited?: Date | string; // Todo use different date object
  authScope?: string; // Todo typesafe
}
