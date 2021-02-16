
import {Place} from '../domain/place';
import {parseISO} from 'date-fns';

export class PlaceFactory {

  static fromRaw(item: Place/*Raw*/): Place {
    return {
      ...item,
      createdAt: PlaceFactory.parseDate(item.createdAt),
      updatedAt: PlaceFactory.parseDate(item.updatedAt),
      lastVisited: PlaceFactory.parseDate(item.lastVisited)
    };
  }

  // todo move central place
  static parseDate(dat: string | Date): Date {
    return dat ? parseISO(dat as string) : null;
  }

}
