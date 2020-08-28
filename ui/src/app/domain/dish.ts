export interface Dish {
  id: string;
  name: string;
  areaCode: string;
  summary?: string;
  imageUrl?: string;
  // lon/länge, lat/breite
  tags?: string[];
}

