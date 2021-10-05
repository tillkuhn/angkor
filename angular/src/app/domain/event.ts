import {ManagedEntity} from '@shared/domain/entities';
/*
{
  "id": "982fc5b6-b0e9-4207-bf73-712fd62de83f",
  "action": "update:place",
  "message": "Update Place Nice Island (my)",
  "topic": "app",
  "entityId": "b18a615b-077b-4751-9ad2-153905343405",
  "source": "angkor-api",
  "partition": 1,
  "offset": 39,
  "time": "2021-06-01T13:06:49.063Z",
  "userId": "39134950-97ef-4961-a4b1-96b1bacc8b9c"
}
 */

// Same props for API and UI Entity
interface GenericEvent extends ManagedEntity {
  id?: string;
  action: string;
  message: string;
  topic: string;
  entityId?: string;
  source: string;
  partition: number;
  offset: number;
  userId?: string;
}

// Structure we use in UI
export interface Event extends GenericEvent {
  time?: Date;
}

// Structure returned from /api
export interface ApiEvent extends GenericEvent {
  time?: string;
}
