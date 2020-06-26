export class Place {
  id: string;
  name: string;
  country: string;
  summary: string;
  imageUrl: string;
  // lon/länge, lat/breite
  coordinates?: number[];
}
