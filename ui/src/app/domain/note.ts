import {Moment} from 'moment';

export interface Note {
  id: string;
  summary: string;
  status: string;
  createdAt: Moment;
  dueDate: Moment;
  createdBy: string;
  tags: string[];
  authScope?: string; // Todo typesafe
}

export const NOTE_TAGS: string[] = ['new', 'urgent', 'place', 'dish', 'music'];
