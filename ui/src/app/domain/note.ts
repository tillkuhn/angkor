import { Moment} from 'moment';

export class Note {
  id: string;
  summary: string;
  status: string;
  createdAt: Moment;
  createdBy: string;
  tags?: string[];
}
