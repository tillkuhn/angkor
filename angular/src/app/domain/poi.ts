/**
 * Point of Interest
 */
export interface POI {
  id?: string;
  name: string;
  areaCode?: string;
  imageUrl?: string;
  coordinates?: Array<number>;
  locationType?: string;
}
