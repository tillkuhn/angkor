/*
 * Location types also used in
 * dropdown for places
 */

export class Place {
  id: string;
  name: string;
  areaCode: string;
  summary?: string;
  imageUrl?: string;
  // lon/länge, lat/breite
  locationType?: string;
  coordinates?: number[];
}

// https://basarat.gitbook.io/typescript/type-system/index-signatures#typescript-index-signature
export interface LocationType {
  label: string;
  icon: string;
  value: string;
}

// icon should match https://material.io/resources/icons/
export const LOCATION_TYPES: { [index: string]: LocationType } = {

  PLACE: {label: 'Place', icon: 'place', value: 'PLACE'},
  ACCOM: {label: 'Accomodation', icon: 'hotel',value: 'ACCOM'},
  BEACH: {label: 'Beach & Island', icon: 'beach_access',value: 'BEACH'},
  CITY: {label: 'Citytrip', icon: 'location_city',value: 'CITY'},
  EXCURS: {label: 'Excursion & Activities', icon: 'directions_walk',value: 'EXCURS'},
  MONUM: {label: 'Monument', icon: 'account_balance',value: 'MONUM'},
  MOUNT: {label: 'Mountain & Skiing', icon: 'ac_unit',value: 'MOUNT'},
  ROAD: {label: 'Roadtrip Destination', icon: 'directions:car',value: 'ROAD'}
}
