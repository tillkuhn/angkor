import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {NGXLogger} from 'ngx-logger';
import {Observable, Subject} from 'rxjs';
import {Area} from '../domain/area';
import {environment} from '../../environments/environment';
import {shareReplay, tap} from 'rxjs/operators';
import {ListItem} from '../domain/list-item';

const CACHE_SIZE = 1;

export enum ListType {
  NOTE_STATUS,
  AUTH_SCOPE
}

@Injectable({
  providedIn: 'root'
})
// inspired by https://blog.thoughtram.io/angular/2018/03/05/advanced-caching-with-rxjs.html
export class MasterDataService {

  private datastore: Map<ListType, Map<string, ListItem>>;

  private countriesCache$: Observable<Area[]>;
  private reload$ = new Subject<void>();

  private locationTypes: Array<ListItem>;
  private locationTypesLookup: Map<string, number> = new Map();


  constructor(private http: HttpClient, private logger: NGXLogger) {
    this.datastore = new Map<ListType, Map<string, ListItem>>();
    Object.keys(ListType).filter(
      key => !isNaN(Number(ListType[key]))
    ).forEach(
      entry => this.datastore.set(ListType[entry], new Map<string, ListItem>())
    );
    this.addStaticListItem(ListType.NOTE_STATUS, {label: 'Open', icon: 'new_releases', value: 'OPEN'});
    this.addStaticListItem(ListType.NOTE_STATUS, {label: 'In progress', icon: 'pending', value: 'IN_PROGRESS'});
    this.addStaticListItem(ListType.NOTE_STATUS, {label: 'Impeded', icon: 'security', value: 'IMPEDED'});
    this.addStaticListItem(ListType.NOTE_STATUS, {label: 'Closed', icon: 'cancel', value: 'CLOSED'});

    // todo export declare type AuthScope = 'PUBLIC' | 'ALL_AUTH' | 'PRIVATE';
    this.addStaticListItem(ListType.AUTH_SCOPE, {label: 'Public', icon: 'lock_open', value: 'PUBLIC'});
    this.addStaticListItem(ListType.AUTH_SCOPE, {label: 'Authenticated', icon: 'lock', value: 'ALL_AUTH'});
    this.addStaticListItem(ListType.AUTH_SCOPE, {label: 'Private', icon: 'security', value: 'PRIVATE'});

    this.locationTypes = [
      {label: 'Place', icon: 'place', value: 'PLACE'},
      {label: 'Accomodation', icon: 'hotel', value: 'ACCOM'},
      {label: 'Bar & Food', icon: 'restaurant', value: 'BARFOOD'},
      {label: 'Beach & Island', icon: 'beach_access', value: 'BEACH'},
      {label: 'Biketrip', icon: 'directions_bike', value: 'BIKE'},
      {label: 'Citytrip', icon: 'location_city', value: 'CITY'},
      {label: 'Excursion & Activities', icon: 'directions_walk', value: 'EXCURS'},
      {label: 'Monument', icon: 'account_balance', value: 'MONUM'},
      {label: 'Mountain & Skiing', icon: 'ac_unit', value: 'MOUNT'},
      {label: 'Roadtrip Destination', icon: 'directions:car', value: 'ROAD'}
    ];
    this.locationTypes.forEach((item, i) => this.locationTypesLookup.set(item.value, i));
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

  getList(listType: ListType): Array<ListItem> {
    return Array.from(this.datastore.get(listType).values());
  }

  getListItem(listType: ListType, key: string): ListItem {
    return this.datastore.get(listType).get(key);
  }

  getAuthScope(key: string) {
    return this.getListItem(ListType.AUTH_SCOPE, key);
  }

  getLocationTypes() {
    return this.locationTypes;
  }

  lookupLocationType(itemValue: string) {
    // this.logger.debug('checl loc ' +itemValue);
    return this.locationTypes[this.locationTypesLookup.get(itemValue)];
  }

  forceReload() {
    // Calling next will complete the current cache instance
    this.reload$.next();

    // Setting the cache to null will create a new cache the next time 'countries' is called
    this.countriesCache$ = null;
    this.logger.debug('all caches invalidated');
  }

  private addStaticListItem(listType: ListType, listItem: ListItem) {
    this.datastore.get(listType).set(listItem.value, listItem);
  }

  private requestCountries(): Observable<Area[]> {
    return this.http.get<Area[]>(`${environment.apiUrlRoot}/countries`)
      .pipe(
        tap(items => this.logger.debug(`MasterDataService fetched ${items.length} countries from server`))
        /*, catchError(this.handleError('getCountries', []))*/
      );
  }

}
