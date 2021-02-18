export interface Note {
  id: string;
  summary: string;
  status: string;
  dueDate: Date;
  createdAt: Date;
  createdBy: string;
  primaryUrl: string;
  tags: string[];
  authScope?: string; // Todo typesafe
}

export const NOTE_TAGS: string[] = ['new', 'urgent', 'place', 'dish', 'music'];
