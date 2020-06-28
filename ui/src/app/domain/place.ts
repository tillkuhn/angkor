export class Place {
  id: string;
  name: string;
  country: string;
  summary: string;
  imageUrl: string;
  // lon/länge, lat/breite
  coordinates?: number[];
}

/*
 * Location types also used in
 * dropdown for places
 */
export enum LocationType {
  PLACE = 'Place (default)',
  ACCOM = 'Accomodation',
  BEACH = 'Beach & Island',
  CITY = 'Citytrip',
  EXCURS = 'Excursion & Activities',
  MONUM = 'Monument',
  MOUNT = 'Mountain & Skiing',
  ROAD = 'Roadtrip Destination'
}
