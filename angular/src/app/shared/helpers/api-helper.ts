import {format, formatISO, parseISO} from 'date-fns';
import {EntityType} from '@shared/domain/entities';
import {environment} from '../../../environments/environment';
import {Observable, of} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';
import {EntityEventService} from '@shared/services/entity-event.service';

/**
 * Static helper methods for dealing with Entities
 */
export class ApiHelper {

  /**
   * Convert iso date string to ts date object
   */
  static parseISO(dateString: string): Date {
    return dateString ? parseISO(dateString) : null;
  }

  static formatISO(date: Date): string {
    return date ? formatISO(date) : null;
  }

  static formatISOasShortDate(date: Date): string {
    return date ? format(date, 'yyyy-MM-dd') : null;
  }


  // static functions must come on top
  static getApiUrl(entityType: EntityType) {
    return `${environment.apiUrlRoot}/${ApiHelper.getApiPath(entityType)}`;
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
      case EntityType.LINK:
        path = 'links';
        break;
      default:
        throw new Error(`No path mapping for ${entityType}`);
    }
    return path;
  }

  /**
   * Generic Handler for Http operations that failed, returns a lambda function that
   * logs / notifies on the error, and let the app continue with a proxy response
   * such as [] for an API function that expects an array of items
   *
   * @param operation - name of the operation that failed
   * @param notifier - optional Notifier instance, defaults to SimpleConsoleNotifier
   * @param result - optional value to return as the observable result
   */
  // notifier: Notifier = new SimpleConsoleNotifier()
  static handleError<T>(operation = 'operation', events: EntityEventService, result?: T) {
    return (error: any): Observable<T> => {

      // IMPROVEMENT: send the error to remote logging infrastructure
      // this.logger.error(`${(this.className)}.${operation}  failed: ${error.message}`); // or log full $error ???
      let errorMsg: string;
      if (error instanceof HttpErrorResponse) {
        // this.logger.warn('HttpErrorResponse message:', e.message, 'status:', e.status);
        if (error.status === 403) { // Forbidden
          errorMsg = `Access to item is forbidden (${error.status}), maybe your are not authenticated?`;
        } else if (error.status === 404) { // Not found
          errorMsg = `️Item not found (${error.status}), maybe you got the wrong Item Id?`;
        } else if (error.status === 400) { // Not found
          errorMsg = `The server thinks you made a Bad Request (${error.status})!`;
        } else if (error.status >= 500 && error.status < 599) { // Gateway Timeout
          errorMsg = `Unexpected HTTP Server Error (${error.status}). We\'re really sorry!'`;
        }
      } else {
        // Not an HttpErrorResponse
        errorMsg = `Unexpected Server Error (${error}). We\'re really sorry!'`;
      }
      events.emitError({message: errorMsg, error, operation});
      // IMPROVEMENT? in some cases also reroute: https://stackoverflow.com/a/56971256/4292075 ???
      // .onAction().subscribe(() => this.router.navigateByUrl('/app/user/detail'));
      // Let the app keep running by returning an empty (but typed) result.
      return of(result); // as T
    };
  }

}
