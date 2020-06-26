import { Injectable } from '@angular/core';
import { Observable, of, throwError } from 'rxjs';
import { HttpClient, HttpHeaders, HttpErrorResponse } from '@angular/common/http';
import { catchError, tap, map } from 'rxjs/operators';
import { Place } from './domain/place';
import { environment} from '../environments/environment';

const httpOptions = {
  headers: new HttpHeaders({'Content-Type': 'application/json'})
};
const apiUrl = environment.apiUrlRoot + '/places';
@Injectable({
  providedIn: 'root'
})
export class ApiService {

  constructor(private http: HttpClient) { }

  getPlaces(): Observable<Place[]> {
    return this.http.get<Place[]>(apiUrl)
      .pipe(
        tap(place => console.log('fetched places')),
        catchError(this.handleError('getPlaces', []))
      );
  }

  getPlace(id: number): Observable<Place> {
    const url = `${apiUrl}/${id}`;
    return this.http.get<Place>(url).pipe(
      tap(_ => console.log(`fetched place id=${id}`)),
      catchError(this.handleError<Place>(`getPlace id=${id}`))
    );
  }

  addPlace(place: Place): Observable<Place> {
    return this.http.post<Place>(apiUrl, place, httpOptions).pipe(
      tap((prod: any) => console.log(`added place w/ id=${prod.id}`)),
      catchError(this.handleError<Place>('addPlace'))
    );
  }

  updatePlace(id: any, place: Place): Observable<any> {
    const url = `${apiUrl}/${id}`;
    return this.http.put(url, place, httpOptions).pipe(
      tap(_ => console.log(`updated place id=${id}`)),
      catchError(this.handleError<any>('updatePlace'))
    );
  }

  deletePlace(id: any): Observable<Place> {
    const url = `${apiUrl}/${id}`;
    return this.http.delete<Place>(url, httpOptions).pipe(
      tap(_ => console.log(`deleted place id=${id}`)),
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
