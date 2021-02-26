import {format, formatISO, parseISO} from 'date-fns';
import {EntityType} from '../domain/entities';
import {environment} from '../../environments/environment';

/**
 * Static helper methods for dealing with Entities
 */
export class EntityHelper {

  /**
   * Convert iso date string to ts date object
   */
  static parseISO(dateString: string): Date {
    return dateString ? parseISO(dateString as string) : null;
  }

  static formatISO(date: Date): string {
    return date ? formatISO(date) : null;
  }

  static formatISOasShortDate(date: Date): string {
    return date ? format(date, 'yyyy-MM-dd') : null;
  }


  // static functions must come on top
  static getApiUrl(entityType: EntityType) {
    return `${environment.apiUrlRoot}/${EntityHelper.getApiPath(entityType)}`;
  }

  // Returns the right root path for a given EntityType
  static getApiPath(entityType: EntityType) {
    let path: string;
    switch (entityType) {
      case EntityType.Place:
        path = 'places';
        break;
      case EntityType.Dish:
        path = 'dishes';
        break;
      case EntityType.Note:
        path = 'notes';
        break;
      case EntityType.Area:
        path = 'areas';
        break;
      case EntityType.Tag:
        path = 'tags';
        break;
      default:
        throw new Error(`No path mapping for ${entityType}`);
    }
    return path;
  }

}
