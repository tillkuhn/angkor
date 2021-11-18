import {EntityType} from '@shared/domain/entities';

/**
 * Point of Interest
 *   {
 *   "id": "d5685229-2690-44ef-8772-fb0616dfe2d9",
 *   "name": "Fish Mekong Delta",
 *   "imageUrl": "https://img.youtube.com/vi/134.jpg",
 *   "entityType": "Post",
 *   "coordinates": [
 *     105.746854,
 *     10.045162
 *   ]
 * },
 */
export interface POI {
  id?: string;
  name: string;
  areaCode?: string;
  imageUrl?: string;
  coordinates?: Array<number>;
  locationType?: string;
  entityType?: EntityType;
}
