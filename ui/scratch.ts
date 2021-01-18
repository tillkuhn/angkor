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
import {map} from 'rxjs/operators';

export declare const enum SomeState {
  OPEN = 0,
  CLOSED = 2
}

interface Place {
  name: string;
  country?: string;
  summary?: string;
}

export enum ListType {
  NOTE_STATUS,
  AUTH_SCOPES,
  LOCATION_TYPE
}

export declare type EntityTypePath = 'places' | 'notes' | 'dishes';

export class Service {
  constructor() {
    console.log('hossa');
  }

  // https://www.typescriptlang.org/docs/handbook/generics.html
  getItem<T>(): T {
    const a: any = {name: 'Hintertupfingen'} ;
    return a;
  }
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

  // https://www.google.de/maps/place/Sam+The+Man+Dawei+Tours/@14.0641366,98.1814599,11.24z
  static parseCoordinates(mapsurl: string): string {
    const regexpCoordinates = /(-?[0-9\.]+)[,\s]+(-?[0-9\.]+)/;
    const match = mapsurl.match(regexpCoordinates);
    if (match == null) {
      throw Error(mapsurl + 'does not match ' + regexpCoordinates);
    }
    return `${match[1]},${match[2]}`;
  }

  static extractLinks(input: string): any {
    const linkRegexp = /(.*?)(https?:\/\/[^\s]+)(.*)/;
    const linkMatches = input.match(linkRegexp);
    if (linkMatches == null) {
      return input;
    } else {
      const dommi = linkMatches[2].match(/(?:https?:\/\/)?(?:[^@\/\n]+@)?(?:www\.)?([^:\/?\n]+)/);
      return linkMatches[1] + dommi[1]  + linkMatches[3];
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

////////////////////////////////////////////////////////////
// Fire away, this is your "main" section :-)
//////////////////////////////////////////////////////////////

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

// ---------------------------------------------------------------------------
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
console.log(Utils.parseCoordinates('https://www.google.de/maps/place/Sam+The+Man+Dawei+Tours/@14.0641366,98.1814599,11.24z/data='));
console.log(Utils.parseCoordinates('https://www.google.de/maps/place/Pean+Bang,+Kambodscha/@12.7405572,104.2501473,16z/data=!3m1!4b1!4m13!1m7!3m6!1s0x30e50bc719b6f191:0xa7c45ea8fdf2bb4a!2sDawei,+Myanmar+(Birma)!3b1!8m2!3d14.082769!4d98.193961!3m4!1s0x310fb4dee39d6201:0x5fdcd85cc98e5184!8m2!3d12.7374703!4d104.2575073'));
console.log(Utils.parseCoordinates('12.44,33.4'));
console.log(Utils.parseCoordinates('12.44, 33.5'));
console.log(Utils.parseCoordinates('12.2211 33.5'));
let str = 'hase';
try {
   str = Utils.parseCoordinates('horst');
} catch (e) {
  console.log(e.message);
}
console.log(Utils.parseCoordinates('https://www.google.com/maps/place/Lanzarote/@29.0398299,-13.7907593,11z/data=!4m13!1m7!3m6!1s0x'));

const svc = new Service();
console.log(svc.getItem<Place>());
const link = 'hallo https://www.hase.de/schorsch?mimi=mi klaus 2. http://horst.de';
console.log(link, ':', Utils.extractLinks(link));
console.log('Short:', Utils.extractLinks('http://klaus.de'));
