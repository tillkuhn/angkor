/*
 * Location types also used in
 * dropdown for places
 */

export class Place {
  id: string;
  name: string;
  country: string;
  summary?: string;
  imageUrl?: string;
  // lon/länge, lat/breite
  lotype?: string;
  coordinates?: number[];
}


