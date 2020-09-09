import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {Observable, Subject} from 'rxjs';
import {Area} from '../domain/area';
import {environment} from '../../environments/environment';
import {shareReplay, tap} from 'rxjs/operators';
import {ListItem} from '../domain/shared';

const CACHE_SIZE = 1;

@Injectable({
  providedIn: 'root'
})
// inspired by https://blog.thoughtram.io/angular/2018/03/05/advanced-caching-with-rxjs.html
export class MasterDataService {

  private countriesCache$: Observable<Area[]>;
  private reload$ = new Subject<void>();

  private authScopes: Array<ListItem>;
  private authScopesLookup: Map<string, number> = new Map();

  private locationTypes: Array<ListItem>;
  private locationTypesLookup: Map<string, number> = new Map();


  constructor(private http: HttpClient, private logger: NGXLogger) {
    // todo export declare type AuthScope = 'PUBLIC' | 'ALL_AUTH' | 'PRIVATE';
    this.authScopes = [
      {label: 'Public', icon: 'lock_open', value: 'PUBLIC'},
      {label: 'Authenticated', icon: 'lock', value: 'ALL_AUTH'},
      {label: 'Private', icon: 'security', value: 'PRIVATE'}
      ];
    this.authScopes.forEach( (item, i) => this.authScopesLookup.set(item.value, i ));

    this.locationTypes = [
     {label: 'Place', icon: 'place', value: 'PLACE'},
     {label: 'Accomodation', icon: 'hotel', value: 'ACCOM'},
     {label: 'Beach & Island', icon: 'beach_access', value: 'BEACH'},
     {label: 'Citytrip', icon: 'location_city', value: 'CITY'},
     {label: 'Excursion & Activities', icon: 'directions_walk', value: 'EXCURS'},
     {label: 'Monument', icon: 'account_balance', value: 'MONUM'},
     {label: 'Mountain & Skiing', icon: 'ac_unit', value: 'MOUNT'},
      {label: 'Roadtrip Destination', icon: 'directions:car', value: 'ROAD'}
    ];
    this.locationTypes.forEach( (item, i) => this.locationTypesLookup.set(item.value, i ));
  }

  getAuthScopes() {
    return this.authScopes;
  }

  /**
   *  Can use like this: <mat-icon>{{masterDataService.lookupAuthscope('ALL_AUTH').icon}}</mat-icon>
   */
  lookupAuthscope(itemValue: string) {
    // todo handle undefined if called too early or key does not exist
    // this.logger.debug('checl auth ' +itemValue);
    return this.authScopes[this.authScopesLookup.get(itemValue)];
  }

  getLocationTypes() {
    return this.locationTypes;
  }

  lookupLocationType(itemValue: string) {
    // this.logger.debug('checl loc ' +itemValue);
    return this.locationTypes[this.locationTypesLookup.get(itemValue)];
  }

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
    return this.http.get<Area[]>(`${environment.apiUrlRoot}/countries`)
      .pipe(
        tap(items => this.logger.debug(`MasterDataService fetched ${items.length} countries from server`))
        /*, catchError(this.handleError('getCountries', []))*/
      );
  }

  forceReload() {
    // Calling next will complete the current cache instance
    this.reload$.next();

    // Setting the cache to null will create a new cache the next time 'countries' is called
    this.countriesCache$ = null;
    this.logger.debug('all caches invalidated');
  }

}
