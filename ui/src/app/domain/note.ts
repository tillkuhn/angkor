import {ManagedEntity} from './entities';

export interface GenericNote extends ManagedEntity {
  id: string;
  summary: string;
  status: string;
  createdBy: string;
  primaryUrl: string;
  tags: string[];
  authScope?: string; // Todo typesafe
  assignee?: string;
}

export interface Note extends GenericNote {
  createdAt?: Date;
  dueDate?: Date;
}

export interface ApiNote extends GenericNote {
  createdAt?: string;
  dueDate?: string;
}

export const NOTE_TAGS: string[] = ['new', 'urgent', 'place', 'dish', 'music'];
