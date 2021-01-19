export interface Note {
  id: string;
  summary: string;
  status: string;
  createdAt: Date | string;
  dueDate: Date | string;
  createdBy: string;
  primaryUrl: string;
  tags: string[];
  authScope?: string; // Todo typesafe
}

export const NOTE_TAGS: string[] = ['new', 'urgent', 'place', 'dish', 'music'];
