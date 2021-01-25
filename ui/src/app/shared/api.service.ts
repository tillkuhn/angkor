import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {HttpClient, HttpErrorResponse, HttpHeaders} from '@angular/common/http';
import {catchError, map, tap} from 'rxjs/operators';
import {Place} from '../domain/place';
import {environment} from '../../environments/environment';
import {NGXLogger} from 'ngx-logger';
import {Area} from '../domain/area';
import {POI} from '../domain/poi';
import {Dish} from '../domain/dish';
import {Note} from '../domain/note';
import {Metric} from '../admin/metrics/metric';
import {AreaNode} from '../domain/area-node';
import {EntityType} from '../domain/entities';
import {format, parseISO} from 'date-fns';
import {MatSnackBar} from '@angular/material/snack-bar';

const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};


@Injectable({
  providedIn: 'root'
})
export class ApiService {

  apiUrlPlaces = ApiService.getApiUrl(EntityType.PLACE);
  apiUrlNotes = ApiService.getApiUrl(EntityType.NOTE);
  apiUrlDishes = ApiService.getApiUrl(EntityType.DISH);
  apiUrlAreas = ApiService.getApiUrl(EntityType.AREA);

  constructor(private http: HttpClient,
              private snackBar: MatSnackBar,
              private logger: NGXLogger) {
  }

  // static funtions must come on top
  static getApiUrl(entityType: EntityType) {
    return `${environment.apiUrlRoot}/${ApiService.getApiPath(entityType)}`;
  }

  // Returns the right root path for a given EntityType
  static getApiPath(entityType: EntityType) {
    let path: string;
    switch (entityType) {
      case EntityType.PLACE:
        path = 'places';
        break;
      case EntityType.DISH:
        path = 'dishes';
        break;
      case EntityType.NOTE:
        path = 'notes';
        break;
      case EntityType.AREA:
        path = 'areas';
        break;
      default:
        throw new Error(`No path mapping for ${entityType}`);
    }
    return path;
  }


  /**
   * Area codes, countries, PoIs  and regions
   */
  getCountries(): Observable<Area[]> {
    return this.http.get<Area[]>(environment.apiUrlRoot + '/countries')
      .pipe(
        // tap: Perform a side effect for every emission on the source Observable, but return an Observable that is identical to the source.
        tap(place => this.logger.debug('ApiService fetched countries')),
        catchError(this.handleError('getCountries', []))
      );
  }

  getAreaTree(): Observable<AreaNode[]> {
    return this.http.get<AreaNode[]>(environment.apiUrlRoot + '/area-tree')
      .pipe(
        // tap: Perform a side effect for every emission on the source Observable, but return an Observable that is identical to the source.
        tap(item => this.logger.debug('ApiService fetched getAreaTree')),
        catchError(this.handleError('getAreaTree', []))
      );
  }

  addArea(area: Area): Observable<Area> {
    return this.http.post<Area>(this.apiUrlAreas, area, httpOptions).pipe(
      tap((prod: any) => this.logger.debug(`added area w/ id=${prod.id}`)),
      catchError(this.handleError<Place>('addArea()'))
    );
  }


  getPOIs(): Observable<POI[]> {
    return this.http.get<POI[]>(environment.apiUrlRoot + '/pois')
      .pipe(
        tap(pois => this.logger.debug(`ApiService.getPOIs fetched ${pois.length} pois`)),
        catchError(this.handleError('getPOIs', []))
      );
  }

  /*
   * Yummy Dishes
   */
  getDishes(search: string): Observable<Dish[]> {
    return this.http.get<Dish[]>(`${this.apiUrlDishes}/search/${search}`)
      .pipe(
        map(items =>
          items.map(item => this.fromRawDish(item)),
        ),
        tap(dish => this.logger.debug(`fetched ${dish.length} dishes  `)),
        catchError(this.handleError('getDishes', []))
      );
  }

  // Details of a single place
  getDish(id: number): Observable<Dish> {
    const url = `${this.apiUrlDishes}/${id}`;
    return this.http.get<Dish>(url).pipe(
      map(item => this.fromRawDish(item)),
      tap(_ => this.logger.debug(`fetched dish id=${id}`)),
      catchError(this.handleError<Dish>(`getDish id=${id}`))
    );
  }

  updateDish(id: any, item: Dish): Observable<any> {
    const op = 'apiService.updateDish';
    const url = `${this.apiUrlDishes}/${id}`;
    return this.http.put(url, item, httpOptions).pipe(
      tap(_ => this.logger.debug(`${op} id=${id}`)),
      catchError(this.handleError<any>('${op}'))
    );
  }

  // Ad new Dish is born
  addDish(item: Dish): Observable<Dish> {
    return this.http.post<Dish>(this.apiUrlDishes, item, httpOptions).pipe(
      tap((result: any) => this.logger.debug(`added dish with id=${result.id}`)),
      catchError(this.handleError<Dish>('addDish'))
    );
  }

  deleteDish(id: any): Observable<Dish> {
    const url = `${this.apiUrlDishes}/${id}`;
    return this.http.delete<Dish>(url, httpOptions).pipe(
      tap(_ => this.logger.debug(`deleted dish id=${id}`)),
      catchError(this.handleError<Dish>('deleteDish'))
    );
  }

  justServed(id: string): Observable<any> {
    return this.http.put<Note>(`${this.apiUrlDishes}/${id}/just-served`, httpOptions).pipe(
      tap((resp: any) => this.logger.debug(`just served dish result=${resp.result}`)),
      catchError(this.handleError<Place>('justServed'))
    );
  }

  /*
   * Important Notes
   */
  getNotes(search: string): Observable<Note[]> {
    return this.http.get<Note[]>(`${this.apiUrlNotes}/search/${search}`)
      .pipe(
        map(items =>
          items.map(item => this.fromRawNote(item)),
        ),
        tap(note => this.logger.debug('ApiService fetched notes')),
        catchError(this.handleError('getNotes', []))
      );
  }

  addNote(item: Note): Observable<Note> {
    return this.http.post<Note>(this.apiUrlNotes, this.toRawNote(item), httpOptions).pipe(
      map(newItem => this.fromRawNote(newItem)),
      tap((note: any) => this.logger.debug(`added note w/ id=${note.id}`)),
      catchError(this.handleError<Place>('addItem'))
    );
  }

  updateNote(id: string, item: Note): Observable<any> {
    const op = 'apiService.updateNote';
    const url = `${this.apiUrlNotes}/${id}`;
    return this.http.put(url, this.toRawNote(item), httpOptions).pipe(
      tap(_ => this.logger.debug(`${op} id=${id}`)),
      catchError(this.handleError<any>('${op}'))
    );
  }

  deleteNote(id: any): Observable<Note> {
    const url = `${this.apiUrlNotes}/${id}`;
    return this.http.delete<Note>(url, httpOptions).pipe(
      tap(_ => this.logger.debug(`deleted note id=${id}`)),
      catchError(this.handleError<Note>('deleteNote'))
    );
  }

  /*
   * Places to go
   */
  getPlaces(search: string): Observable<Place[]> {
    return this.http.get<Place[]>(`${this.apiUrlPlaces}/search/${search}`)
      .pipe(
        map(items =>
          items.map(item => this.fromRawPlace(item)),
        ),
        tap(item => this.logger.debug(`fetched ${item.length} places  `)),
        catchError(this.handleError('getPlaces', []))
      );
  }

  // Details of a single place
  getPlace(id: number): Observable<Place> {
    const url = `${this.apiUrlPlaces}/${id}`;
    return this.http.get<Place>(url).pipe(
      map(item => this.fromRawPlace(item)),
      tap(_ => this.logger.debug(`fetched place id=${id}`)),
      catchError(this.handleError<Place>(`getPlace id=${id}`))
    );
  }

  updatePlace(id: any, place: Place): Observable<any> {
    const url = `${this.apiUrlPlaces}/${id}`;
    // const apiPlace = { ...place, authScope: (place.authScope as ListItem).value}
    return this.http.put(url, place, httpOptions).pipe(
      tap(_ => this.logger.debug(`updated place id=${id}`)),
      catchError(this.handleError<any>('updatePlace'))
    );
  }

  addPlace(place: Place): Observable<Place> {
    return this.http.post<Place>(this.apiUrlPlaces, place, httpOptions).pipe(
      tap((prod: any) => this.logger.debug(`added place w/ id=${prod.id}`)),
      catchError(this.handleError<Place>('addPlace'))
    );
  }

  deletePlace(id: any): Observable<Place> {
    const url = `${this.apiUrlPlaces}/${id}`;
    return this.http.delete<Place>(url, httpOptions).pipe(
      tap(_ => this.logger.debug(`deleted place id=${id}`)),
      catchError(this.handleError<Place>('deletePlace'))
    );
  }

  getMetrics(): Observable<Metric[]> {
    return this.http.get<Metric[]>(`${environment.apiUrlRoot}/admin/metrics`)
      .pipe(
        tap(metrics => this.logger.debug(`svc fetched ${metrics.length} metrics`)),
        catchError(this.handleError('getDishes', []))
      );
  }

  /**
   * Generic Error Handler
   */
  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // IMPROVEMENT: send the error to remote logging infrastructure
      if (error instanceof HttpErrorResponse) {
        const e = error as HttpErrorResponse;
        this.logger.info('message:', e.message, 'status:', e.status);
        if (e.status === 403) {
          this.snackBar.open('Access to item is forbidden, check if you are authenticated!',
            'Acknowledge', {duration: 5000});
        }
      }
      this.logger.error('error during operation', operation, error); // log to console instead

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  /**
   * factory methods for conversion from raw json to our domain model, mostly date conversion
   */
  fromRawPlace(item: Place/*Raw*/): Place {
    return {
      ...item,
      createdAt: this.parseDate(item.createdAt),
      updatedAt: this.parseDate(item.updatedAt),
      lastVisited: this.parseDate(item.lastVisited)
    };
  }

  fromRawDish(item: Dish/*Raw*/): Dish {
    return {
      ...item,
      createdAt: this.parseDate(item.createdAt),
      updatedAt: this.parseDate(item.updatedAt)
    };
  }

  fromRawNote(item: Note/*Raw*/): Note {
    return {
      ...item,
      createdAt: this.parseDate(item.createdAt),
      dueDate: this.parseDate(item.dueDate),
    };
  }

  toRawNote(item: Note /*UI*/): any {
    return {
      ...item,
      dueDate: item.dueDate ? format(item.dueDate as Date, 'yyyy-MM-dd') : null
    };
  }

  parseDate(dat: string | Date): Date {
    return dat ? parseISO(dat as string) : null;
  }

}
