/**
 * This is just a scratch file to execute plan typescript from the shell
 * yarn global add ts-node
 * ts-node scratch.js
 */
import {SmartCoordinates} from './src/app/domain/smart-coordinates';
export declare const enum SomeState {
  OPEN = 0,
  CLOSED = 2
}

interface Place {
  name: string;
  country: string;
  summary?: string;
}
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
