import {ManagedEntity} from './entities';

// Same props for API and UI Entity
interface AbstractDish  extends ManagedEntity {
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

export interface Dish extends AbstractDish {
  createdAt?: Date;
  updatedAt?: Date;
}

export interface ApiDish  extends AbstractDish  {
  createdAt?: string;
  updatedAt?: string;
}
