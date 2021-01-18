
export interface Dish {
  id: string;
  name: string;
  areaCode: string;
  summary?: string;
  notes?: string;
  imageUrl?: string;
  primaryUrl?: string;
  tags?: string[];
  authScope?: string;
  createdAt?: Date;
  updatedAt?: Date;
  timesServed: number;
}

