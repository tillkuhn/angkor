import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {catchError, tap} from 'rxjs/operators';
import {Place} from '../domain/place';
import {environment} from '../../environments/environment';
import {NGXLogger} from 'ngx-logger';
import {Area} from '../domain/area';
import {POI} from '../domain/poi';
import {Dish} from '../domain/dish';
import {Note} from '../domain/note';
import {Metric} from '../admin/metrics/metric';
import {AreaNode} from '../domain/area-node';
import {EntityType} from '../domain/common';

const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};


@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private http: HttpClient,
              private logger: NGXLogger) {
  }

  apiUrlPlaces = ApiService.getApiUrl(EntityType.PLACE);
  apiUrlDishes = ApiService.getApiUrl(EntityType.DISH);
  apiUrlNotes = ApiService.getApiUrl(EntityType.NOTE);
  apiUrlAreas = ApiService.getApiUrl(EntityType.AREA);

  static getApiUrl(entityType: EntityType) {
    let path: string;
    switch (entityType) {
      case EntityType.PLACE: path = 'places'; break;
      case EntityType.DISH: path = 'dishes'; break;
      case EntityType.NOTE: path = 'notes'; break;
      case EntityType.AREA: path = 'areas'; break;
      default: throw new Error(`No path mapping for ${entityType}` );
    }
    return `${environment.apiUrlRoot}/${path}`;
  }


  /**
   * Area codes, countries and regions
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
   * Dishes
   */
  getDishes(search: string): Observable<Dish[]> {
    return this.http.get<Dish[]>(`${this.apiUrlDishes}/search/${search}`)
      .pipe(
        tap(dish => this.logger.debug(`fetched ${dish.length} dishes  `)),
        catchError(this.handleError('getDishes', []))
      );
  }

  // Details of a single place
  getDish(id: number): Observable<Dish> {
    const url = `${this.apiUrlDishes}/${id}`;
    return this.http.get<Dish>(url).pipe(
      tap(_ => this.logger.debug(`fetched dish id=${id}`)),
      catchError(this.handleError<Dish>(`getDish id=${id}`))
    );
  }

  justServed(id: string): Observable<any> {
    return this.http.put<Note>(`${this.apiUrlDishes}/${id}/just-served`, httpOptions).pipe(
      tap((resp: any) => this.logger.debug(`just served dish result=${resp.result}`)),
      catchError(this.handleError<Place>('justServed'))
    );
  }

  /*
   * Notes
   */
  getNotes(): Observable<Note[]> {
    return this.http.get<Note[]>(this.apiUrlNotes)
      .pipe(
        tap(note => this.logger.debug('ApiService fetched notes')),
        catchError(this.handleError('getNotes', []))
      );
  }

  addNote(item: Note): Observable<Note> {
    return this.http.post<Note>(this.apiUrlNotes, item, httpOptions).pipe(
      tap((note: any) => this.logger.debug(`added note w/ id=${note.id}`)),
      catchError(this.handleError<Place>('addItem'))
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
   * Places
   */
  getPlaces(search: string): Observable<Place[]> {
    return this.http.get<Place[]>(`${this.apiUrlPlaces}/search/${search}`)
      .pipe(
        tap(item => this.logger.debug(`fetched ${item.length} places  `)),
        catchError(this.handleError('getPlaces', []))
      );
  }

  // Details of a single place
  getPlace(id: number): Observable<Place> {
    const url = `${this.apiUrlPlaces}/${id}`;
    return this.http.get<Place>(url).pipe(
      // map(placeRaw => this.fromRaw(placeRaw)),
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


  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      this.logger.error(error); // log to console instead

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }


  /* example factory method
  fromRaw(rawPlace: Place): Place {
    const authScopeConst = rawPlace.authScope;
    // replace authScope String with ListItem Object
    return {
      ...rawPlace,
      authScope: this.masterDate.lookupSomething(authScopeConst as string)
    };
  }
   */
}
