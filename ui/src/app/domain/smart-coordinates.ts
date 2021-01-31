/**
 * Smart coorindates, thanks to  https://github.com/perfectline/geopoint
 * The lat(itude) of Bangkok, Thailand is 13.736717, and the lon(gitude) is 100.523186.
 * DD: 13.736717 100.523186
 * DMS: 13° 44' 12.18" N 100° 31' 23.47" E
 * https://latitude.to/lat/13.75633/lng/100.50177
 * https://www.google.com/maps/search/?api=1&query=13.736717,100.523186
 * geojson: [100.523186, 13.736717]
 */

export const REGEXP_COORDINATES = /(-?[0-9\.]+)[,\s]+(-?[0-9\.]+)/;

export class SmartCoordinates {


  static readonly CHAR_DEG = '\u00B0';
  static readonly CHAR_MIN = '\u0027';
  static readonly CHAR_SEC = '\u0022';
  static readonly CHAR_SEP = '\u0020';

  static readonly MAX_LON: 180;
  static readonly MAX_LAT: 90;

  // decimal
  lonDec: number;
  latDec: number;

  // degrees
  lonDeg: string;
  latDeg: string;

  //  GeoJSON position coordinates would be **(Lon,Lat)!!!**
  constructor(lonLat: string | Array<number>) {
    if (typeof lonLat === 'string') {
      // Exmplee from https://latitude.to/
      const coordinates = lonLat.split(/[ ,]+/);
      if (coordinates.length < 2) {
        throw new Error(`${lonLat} String not supported`);
      }
      this.lonDec = parseFloat(coordinates[1]);
      this.latDec = parseFloat(coordinates[0]);
      // Todo support https://www.google.de/maps/place/Flughafen+Bangkok-Suvarnabhumi/@13.7248936,100.4930288,11z/data=
    } else if (lonLat !== undefined && lonLat.length > 1) {
      this.lonDec = lonLat[0];
      this.latDec = lonLat[1];
    } else {
      throw new Error(`Invalid arg ${lonLat}, expected array with 2 numbers ([lon.lat]) or parseable string`);
    }
    // calculate degrees
    this.lonDeg = this.dec2deg(this.lonDec, SmartCoordinates.MAX_LON);
    this.latDeg = this.dec2deg(this.latDec, SmartCoordinates.MAX_LAT);
  }

  get latLonDeg(): string {
    return this.latDeg + ' N ' + this.lonDeg + ' E';
  }

  get gmapsUrl(): string {
    return `https://www.google.com/maps/search/?api=1&query=${this.latDec},${this.lonDec}`;
  }

  get lonLatArray(): number[] {
    return [this.lonDec, this.latDec];
  }

  private dec2deg(value, max): string {
    const sign = value < 0 ? -1 : 1;
    const abs = Math.abs(Math.round(value * 1000000));

    if (abs > (max * 1000000)) {
      return 'NaN';
    }

    const dec = abs % 1000000 / 1000000;
    const deg = Math.floor(abs / 1000000) * sign;
    const min = Math.floor(dec * 60);
    const sec = (dec - min / 60) * 3600;

    let result = '';

    result += deg;
    result += SmartCoordinates.CHAR_DEG;
    result += SmartCoordinates.CHAR_SEP;
    result += min;
    result += SmartCoordinates.CHAR_MIN;
    result += SmartCoordinates.CHAR_SEP;
    result += sec.toFixed(2);
    result += SmartCoordinates.CHAR_SEC;

    return result;

  }

  private deg2dec(value: string) {
    const matches = this.decode(value);
    if (!matches) {
      return NaN;
    }
    const deg = parseFloat(matches[1]);
    const min = parseFloat(matches[2]);
    const sec = parseFloat(matches[3]);
    if (isNaN(deg) || isNaN(min) || isNaN(sec)) {
      return NaN;
    }
    return deg + (min / 60.0) + (sec / 3600);
  }

  private decode(value: string): RegExpMatchArray {
    let pattern = '';

    // deg
    pattern += '(-?\\d+)';
    pattern += SmartCoordinates.CHAR_DEG;
    pattern += '\\s*';

    // min
    pattern += '(\\d+)';
    pattern += SmartCoordinates.CHAR_MIN;
    pattern += '\\s*';

    // sec
    pattern += '(\\d+(?:\\.\\d+)?)';
    pattern += SmartCoordinates.CHAR_SEC;

    return value.match(new RegExp(pattern));
  }

}
