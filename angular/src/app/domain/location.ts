// created with json2ts.com/

// Abstract Location Interface
import {EntityType} from '@shared/domain/entities';

export interface Location {
  id: string;
  externalId: string;
  name: string;
  imageUrl: string;
  primaryUrl: string;
  authScope: string;
  coordinates: number[];
  tags: string[];
  createdAt?: string | Date; // Union type
  createdBy?: string;
  updatedAt?: string | Date;
  updatedBy?: string;
  entityType: EntityType;
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

