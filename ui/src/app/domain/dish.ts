
export interface Dish {
  id: string;
  name: string;
  areaCode: string;
  summary?: string;
  imageUrl?: string;
  primaryUrl?: string;
  // lon/länge, lat/breite
  tags?: string[];
  authScope?: string;
  createdAt?: Date;
  updatedAt?: Date;
  timesServed: number;
}

