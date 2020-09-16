/**
 * This is just a scratch file to execute plan typescript from the shell
 * yarn global add ts-node
 * ts-node scratch.js
 */

// https://github.com/TypeStrong/ts-node/issues/922#issuecomment-673155000 edit tsconig to use import !
import {SmartCoordinates} from './src/app/domain/smart-coordinates';
import {Observable} from 'rxjs';
import {Area} from './src/app/domain/area';
import {ListItem} from './src/app/domain/list-item';

export declare const enum SomeState {
  OPEN = 0,
  CLOSED = 2
}

interface Place {
  name: string;
  country: string;
  summary?: string;
}

export enum ListType {
  NOTE_STATUS,
  AUTH_SCOPES
}

// inspired by https://blog.thoughtram.io/angular/2018/03/05/advanced-caching-with-rxjs.html
export class MasterDataService {

  private datastore: Map<ListType, Map<string, ListItem>>;

  constructor() {
    this.datastore =  new Map<ListType, Map<string, ListItem>>();
    const nos  = new Map<string, ListItem>();
    nos.set('OPEN', {label: 'Public', icon: 'lock_open', value: 'OPEN'});
    nos.set('IN_PROGRESS', {label: 'Authenticated', icon: 'lock', value: 'IN_PROGRESS'});
    // IMPEDED: {label: 'Private', icon: 'security', value: 'IMPEDED'},
    //  CLOSED: {label: 'Private', icon: 'security', value: 'CLOSED'},
    this.datastore.set(ListType.NOTE_STATUS, nos);
    // this.datastore.set(ListType.AUTH_SCOPES, 'auto');
  }
  getList(listType: ListType): Array<ListItem> {
    return Array.from(this.datastore.get(listType).values());
  }

  getListItem(listType: ListType, key: string): ListItem {
    return this.datastore.get(listType).get(key);
  }


}

const mds = new MasterDataService();
console.log(mds.getList(ListType.NOTE_STATUS));
console.log(mds.getListItem(ListType.NOTE_STATUS,'OPEN'));
console.log(mds.getListItem(ListType.NOTE_STATUS,'hase'));

// The lat(itude) of Bangkok, Thailand is 13.736717, and the lon(gitude) is 100.523186.
const point = new SmartCoordinates([100.523186, 13.736717]);
console.log(point.latLonDeg, SomeState.CLOSED );
console.log(point.gmapsUrl);
console.log(new SmartCoordinates([1, 2]));
console.log(new SmartCoordinates('13.75633 100.50177').gmapsUrl);
console.log('lumpi,',new SmartCoordinates('https://www.google.com/maps/place/Lumphini+Park/@13.7314029,100.5392509,17z/data=!4m12!1m6!'));
// console.log(new GeoPoint('horst'));

const place: Place = {name: 'Kuala Lumpur', summary: 'High Towers', country: 'my'};
const place2: Place = {...place, name: 'Georgetown'};
console.log(place, place2);
hase('eins','zwei');

function hase(...args: string[]) {
  console.log(args);
}
