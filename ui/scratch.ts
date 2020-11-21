/**
 * This is just a scratch file to execute plan typescript from the shell
 *
 * yarn global add ts-node
 * ts-node scratch.ts
 *
 * Declare Classes, enums and other types first, then scroll down and add any "main" code you want to execute
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
  AUTH_SCOPES,
  LOCATION_TYPE
}

export class Utils {

  /**
   * Format bytes as human-readable text.
   *
   * Credits: https://stackoverflow.com/questions/10420352/converting-file-size-in-bytes-to-human-readable-string/10420404
   *
   * @param bytes Number of bytes.
   * @param si True to use metric (SI, International System of Units) units, aka powers of 1000.
   *        False to use  binary (IEC), aka powers of 1024.
   * @param dp Number of decimal places to display.
   *
   * @return Formatted string.
   */
  static humanFileSize(bytes: number, si = false, dp = 1): String | Number {
    if ( bytes === null || bytes === undefined ||  Number.isNaN(bytes)) {
      return bytes; // return to sender
    }
    const thresh = si ? 1000 : 1024;

    if (Math.abs(bytes) < thresh) {
      return bytes + ' B';
    }

    const units = si
      ? ['kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB']
      : ['KiB', 'MiB', 'GiB', 'TiB', 'PiB', 'EiB', 'ZiB', 'YiB'];
    let u = -1;
    const r = 10 ** dp;

    do {
      bytes /= thresh;
      ++u;
    } while (Math.round(Math.abs(bytes) * r) / r >= thresh && u < units.length - 1);


    return bytes.toFixed(dp) + ' ' + units[u];
  }

  static truncate(text: string, length: number, fromStart: boolean = false) {
    if (text.length <= length) {
      return text;
    } else if (fromStart) {
      return '\u2026' + text.substr(text.length - length, text.length);
    } else {
      return  text.substr(0, length) + '\u2026';
    }
  }

}

// inspired by https://blog.thoughtram.io/angular/2018/03/05/advanced-caching-with-rxjs.html
export class MasterDataService {

  private datastore: Map<ListType, Map<string, ListItem>>;

  constructor() {
    this.datastore = new Map<ListType, Map<string, ListItem>>();
    const noteStates = new Map<string, ListItem>();
    noteStates.set('OPEN', {label: 'Open', icon: 'new_releases', value: 'OPEN'});
    noteStates.set('IN_PROGRESS', {label: 'In progress', icon: 'pending', value: 'IN_PROGRESS'});
    noteStates.set('IMPEDED', {label: 'Impeded', icon: 'security', value: 'IMPEDED'});
    noteStates.set('CLOSED', {label: 'Closed', icon: 'cancel', value: 'CLOSED'});
    this.datastore.set(ListType.NOTE_STATUS, noteStates);
  }

  getList(listType: ListType): Array<ListItem> {
    return Array.from(this.datastore.get(listType).values());
  }

  getListItem(listType: ListType, key: string): ListItem {
    return this.datastore.get(listType).get(key);
  }


}


// Fire away, this is your "main" section :-)

Object.keys(ListType).filter(key => !isNaN(Number(ListType[key]))).forEach(entry => console.log(entry));

const mds = new MasterDataService();
console.log(mds.getList(ListType.NOTE_STATUS));
console.log(mds.getListItem(ListType.NOTE_STATUS, 'OPEN'));
console.log(mds.getListItem(ListType.NOTE_STATUS, 'hase'));

// The lat(itude) of Bangkok, Thailand is 13.736717, and the lon(gitude) is 100.523186.
const point = new SmartCoordinates([100.523186, 13.736717]);
console.log(point.latLonDeg, SomeState.CLOSED);
console.log(point.gmapsUrl);
console.log(new SmartCoordinates([1, 2]));
console.log(new SmartCoordinates('13.75633 100.50177').gmapsUrl);
console.log('lumpi,', new SmartCoordinates('https://www.google.com/maps/place/Lumphini+Park/@13.7314029,100.5392509,17z/data=!4m12!1m6!'));
// console.log(new GeoPoint('horst'));

const place: Place = {name: 'Kuala Lumpur', summary: 'High Towers', country: 'my'};
const place2: Place = {...place, name: 'Georgetown'};
console.log(place, place2);
hase('eins', 'zwei');

function hase(...args: string[]) {
  console.log(args);
}

// test filesize formatter
console.log(Utils.humanFileSize(1551859712));  // 1.4 GiB
console.log(Utils.humanFileSize(5000, true));  // 5.0 kB
console.log(Utils.humanFileSize(5344000, true));  // 5.3 MB
console.log(Utils.humanFileSize(5000, false));  // 4.9 KiB
console.log(Utils.humanFileSize(undefined)); // ""

// cutter
let truncated;
truncated = Utils.truncate('Hello, World!', 10);
console.log(truncated);
truncated = Utils.truncate('Hello, World!', 50);
console.log(truncated);
truncated = Utils.truncate('Hello, World!', 10, true);
console.log(truncated);
