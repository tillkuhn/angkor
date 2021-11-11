// created with json2ts.com/

// Abstract Location Interface
import {EntityType} from '@shared/domain/entities';

export interface Location {
  id: string;
  externalId: string;
  entityType: EntityType;

  name: string;
  imageUrl: string;
  primaryUrl: string;
  authScope: string;
  coordinates: number[];
  areaCode?: string;
  geoAddress?: string;
  tags: string[];

  createdAt?: string | Date; // Union type
  createdBy?: string;
  updatedAt?: string | Date;
  updatedBy?: string;
}

// Tour
export interface Tour extends Location {
  beenThere?: string | Date;
  rating: number;
}

// Video
export interface Video extends Location {
  summary?: string;
}

// Post
export interface Post extends Location {
  published?: string | Date;
}

