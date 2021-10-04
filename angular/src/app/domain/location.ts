// created with json2ts.com/

// Abstract Location Interface
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
}

// Tour
export interface Tour extends Location {
  beenThere: string | Date;
}
