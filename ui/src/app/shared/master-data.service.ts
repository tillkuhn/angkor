import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {Observable} from 'rxjs';
import {Area} from '../domain/area';
import {environment} from '../../environments/environment';
import {catchError, shareReplay, tap} from 'rxjs/operators';

const CACHE_SIZE = 1;

@Injectable({
  providedIn: 'root'
})
// inspired by https://blog.thoughtram.io/angular/2018/03/05/advanced-caching-with-rxjs.html
export class MasterDataService {

  private countriesCache$: Observable<Array<Area>>;

  constructor(private http: HttpClient,private logger: NGXLogger) { }

  get countries() {
    // This shareReplay operator returns an Observable that shares a single subscription
    // to the underlying source, which is the Observable returned from this.requestCountriesWithRegions()
    if (!this.countriesCache$) {
      this.countriesCache$ = this.requestCountries().pipe(
        shareReplay(CACHE_SIZE)
      );
    }
    return this.countriesCache$;
  }

  private requestCountries(): Observable<Area[]> {
    return this.http.get<Area[]>(environment.apiUrlRoot + '/countries')
      .pipe(
        tap(items => this.logger.debug(`fetched ${items.length} countries from server`))
        /*, catchError(this.handleError('getCountries', []))*/
      );
  }

  invalidateCountries() {
    this.countriesCache$ = undefined;
    this.logger.debug('countryCache invalidated');
  }
}
