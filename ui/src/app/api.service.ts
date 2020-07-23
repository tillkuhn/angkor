import {Injectable} from '@angular/core';
import {Observable, of, throwError} from 'rxjs';
import {HttpClient, HttpHeaders, HttpErrorResponse} from '@angular/common/http';
import {catchError, tap, map} from 'rxjs/operators';
import {Place} from './domain/place';
import {environment} from '../environments/environment';
import {NGXLogger} from 'ngx-logger';
import {Area} from './domain/area';
import {POI} from './domain/poi';
import {Dish} from './domain/dish';
import {Note} from './domain/note';

const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};
const apiUrlPlaces = environment.apiUrlRoot + '/places';
const apiUrlNotes = environment.apiUrlRoot + '/notes';

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private http: HttpClient, private logger: NGXLogger) {
  }

  getCountries(): Observable<Area[]> {
    return this.http.get<Area[]>(environment.apiUrlRoot + '/countries')
      .pipe(
        tap(place => this.logger.debug('fetched countries')),
        catchError(this.handleError('getCountries', []))
      );
  }

  getPOIs(): Observable<POI[]> {
    return this.http.get<POI[]>(environment.apiUrlRoot + '/pois')
      .pipe(
        tap(poi => this.logger.debug('fetched pois')),
        catchError(this.handleError('getPOIs', []))
      );
  }

  getDishes(): Observable<Dish[]> {
    return this.http.get<Dish[]>(environment.apiUrlRoot + '/dishes')
      .pipe(
        tap(dish => this.logger.debug('fetched dishes')),
        catchError(this.handleError('getDishes', []))
      );
  }

  getNotes(): Observable<Note[]> {
    return this.http.get<Note[]>(apiUrlNotes)
      .pipe(
        tap(note => this.logger.debug('fetched notes')),
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
        tap(place => this.logger.debug('fetched places')),
        catchError(this.handleError('getPlaces', []))
      );
  }
  getPlace(id: number): Observable<Place> {
    const url = `${apiUrlPlaces}/${id}`;
    return this.http.get<Place>(url).pipe(
      tap(_ => this.logger.debug(`fetched place id=${id}`)),
      catchError(this.handleError<Place>(`getPlace id=${id}`))
    );
  }

  addPlace(place: Place): Observable<Place> {
    return this.http.post<Place>(apiUrlPlaces, place, httpOptions).pipe(
      tap((prod: any) => this.logger.debug(`added place w/ id=${prod.id}`)),
      catchError(this.handleError<Place>('addPlace'))
    );
  }

  updatePlace(id: any, place: Place): Observable<any> {
    const url = `${apiUrlPlaces}/${id}`;
    return this.http.put(url, place, httpOptions).pipe(
      tap(_ => this.logger.debug(`updated place id=${id}`)),
      catchError(this.handleError<any>('updatePlace'))
    );
  }

  deletePlace(id: any): Observable<Place> {
    const url = `${apiUrlPlaces}/${id}`;
    return this.http.delete<Place>(url, httpOptions).pipe(
      tap(_ => this.logger.debug(`deleted place id=${id}`)),
      catchError(this.handleError<Place>('deletePlace'))
    );
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
