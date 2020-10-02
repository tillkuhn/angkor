import {Injectable} from '@angular/core';
import {Observable, of} from 'rxjs';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {catchError, map, tap} from 'rxjs/operators';
import {Place} from '../domain/place';
import {environment} from '../../environments/environment';
import {NGXLogger} from 'ngx-logger';
import {Area} from '../domain/area';
import {POI} from '../domain/poi';
import {Dish} from '../domain/dish';
import {Note} from '../domain/note';
import {Metric} from '../admin/metrics/metric';
import {MasterDataService} from './master-data.service';
import {ListItem} from '../domain/list-item';

const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};
const apiUrlPlaces = environment.apiUrlRoot + '/places';
const apiUrlDishes = environment.apiUrlRoot + '/dishes';
const apiUrlNotes = environment.apiUrlRoot + '/notes';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private http: HttpClient, private logger: NGXLogger, private masterDate: MasterDataService) {
  }

  getCountries(): Observable<Area[]> {
    return this.http.get<Area[]>(environment.apiUrlRoot + '/countries')
      .pipe(
        // tap: Perform a side effect for every emission on the source Observable, but return an Observable that is identical to the source.
        tap(place => this.logger.debug('ApiService fetched countries')),
        catchError(this.handleError('getCountries', []))
      );
  }

  getPOIs(): Observable<POI[]> {
    return this.http.get<POI[]>(environment.apiUrlRoot + '/pois')
      .pipe(
        tap(poi => this.logger.debug('ApiService fetched pois')),
        catchError(this.handleError('getPOIs', []))
      );
  }

  getDishes(search: string): Observable<Dish[]> {
    return this.http.get<Dish[]>(`${apiUrlDishes}/search/${search}`)
      .pipe(
        tap(dish => this.logger.debug('ApiService fetched dishes')),
        catchError(this.handleError('getDishes', []))
      );
  }


  // Details of a single place
  getDish(id: number): Observable<Dish> {
    const url = `${apiUrlDishes}/${id}`;
    return this.http.get<Dish>(url).pipe(
      tap(_ => this.logger.debug(`fetched dish id=${id}`)),
      catchError(this.handleError<Place>(`getDish id=${id}`))
    );
  }

  getNotes(): Observable<Note[]> {
    return this.http.get<Note[]>(apiUrlNotes)
      .pipe(
        tap(note => this.logger.debug('ApiService fetched notes')),
        catchError(this.handleError('getNotes', []))
      );
  }

  addNote(item: Note): Observable<Note> {
    return this.http.post<Note>(apiUrlNotes, item, httpOptions).pipe(
      tap((note: any) => this.logger.debug(`added note w/ id=${note.id}`)),
      catchError(this.handleError<Place>('addItem'))
    );
  }

  deleteNote(id: any): Observable<Note> {
    const url = `${apiUrlNotes}/${id}`;
    return this.http.delete<Note>(url, httpOptions).pipe(
      tap(_ => this.logger.debug(`deleted note id=${id}`)),
      catchError(this.handleError<Note>('deleteNote'))
    );
  }

  getPlaces(): Observable<Place[]> {
    return this.http.get<Place[]>(apiUrlPlaces)
      .pipe(
        tap(place => this.logger.debug('ApiService fetched places')),
        catchError(this.handleError('getPlaces', []))
      );
  }

  // Details of a single place
  getPlace(id: number): Observable<Place> {
    const url = `${apiUrlPlaces}/${id}`;
    return this.http.get<Place>(url).pipe(
      // map(placeRaw => this.fromRaw(placeRaw)),
      tap(_ => this.logger.debug(`fetched place id=${id}`)),
      catchError(this.handleError<Place>(`getPlace id=${id}`))
    );
  }

  // Todo: move to factory
  /*
  fromRaw(rawPlace: Place): Place {
    const authScopeConst = rawPlace.authScope;
    // replace authScope String with ListItem Object
    return {
      ...rawPlace,
      authScope: this.masterDate.lookupAuthscope(authScopeConst as string)
    };
  }
   */

  updatePlace(id: any, place: Place): Observable<any> {
    const url = `${apiUrlPlaces}/${id}`;
    // const apiPlace = { ...place, authScope: (place.authScope as ListItem).value}
    return this.http.put(url, place, httpOptions).pipe(
      tap(_ => this.logger.debug(`updated place id=${id}`)),
      catchError(this.handleError<any>('updatePlace'))
    );
  }

  addPlace(place: Place): Observable<Place> {
    return this.http.post<Place>(apiUrlPlaces, place, httpOptions).pipe(
      tap((prod: any) => this.logger.debug(`added place w/ id=${prod.id}`)),
      catchError(this.handleError<Place>('addPlace'))
    );
  }

  deletePlace(id: any): Observable<Place> {
    const url = `${apiUrlPlaces}/${id}`;
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
}
