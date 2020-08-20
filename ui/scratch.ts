/**
 * This is just a scratch file to execute plan typescript from the shell
 * yarn global add ts-node
 * ts-node scratch.js
 */
import {GeoPoint} from './src/app/domain/geo-point';

interface Place {
  name: string;
  country: string;
  summary?: string;
}
// The lat(itude) of Bangkok, Thailand is 13.736717, and the lon(gitude) is 100.523186.
const point = new GeoPoint([100.523186, 13.736717]);
console.log(point.latLonDeg );
console.log(point.gmapsUrl);
console.log(new GeoPoint([1, 2]));
console.log(new GeoPoint('13.75633 100.50177').gmapsUrl);
// console.log(new GeoPoint('horst'));

const place: Place = {name: 'Kuala Lumpur', summary: 'High Towers', country: 'my'};
const place2: Place = {...place, name: 'Georgetown'};
console.log(place, place2);
hase('eins','zwei');

function hase(...args: string[]) {
  console.log(args);
}
