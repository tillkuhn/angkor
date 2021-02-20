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
}

export interface Dish extends GenericDish {
  createdAt?: Date;
  updatedAt?: Date;
}

export interface ApiDish  extends GenericDish  {
  createdAt?: string;
  updatedAt?: string;
}
