import {ManagedEntity} from './entities';

// Same props for API and UI Entity
interface GenericDish extends ManagedEntity {
  id: string;
  name: string;
  areaCode: string;
  summary?: string;
  notes?: string;
  imageUrl?: string;
  primaryUrl?: string;
  tags?: string[];
  authScope?: string;
  timesServed: number;
  rating: number;
  createdBy?: string; // todo could be our own enetity with shortname
  updatedBy?: string; // todo could be our own enetity with shortname
}

// Structure we use in UI
export interface Dish extends GenericDish {
  createdAt?: Date;
  updatedAt?: Date;
}

// Structure returned from /api
export interface ApiDish extends GenericDish {
  createdAt?: string;
  updatedAt?: string;
}
