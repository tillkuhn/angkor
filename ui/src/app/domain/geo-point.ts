/**
 * Smart coorindates, thanks to  https://github.com/perfectline/geopoint
 * The lat(itude) of Bangkok, Thailand is 13.736717, and the lon(gitude) is 100.523186.
 */
export class GeoPoint {

  CHAR_DEG = '\u00B0';
  CHAR_MIN = '\u0027';
  CHAR_SEC = '\u0022';
  CHAR_SEP = '\u0020';

  MAX_LON: 180;
  MAX_LAT: 90;

  // decimal
  lonDec: number;
  latDec: number;

  // degrees
  lonDeg: string;
  latDeg: string;

  //  GeoJSON position coordinates would be **(Lon,Lat)!!!**
  constructor(lonLat: Array<number>) {
    if (lonLat !== undefined && lonLat.length > 1) {
      this.lonDeg = this.dec2deg(lonLat[0], this.MAX_LON);
      this.lonDec = lonLat[0];
      this.latDeg = this.dec2deg( lonLat[1], this.MAX_LAT);
      this.latDec = lonLat[1];
    } else {
      console.log('Except number array with len 2, not ' + lonLat );
    }
  }

  dec2deg(value, max): string {
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
    result += this.CHAR_DEG;
    result += this.CHAR_SEP;
    result += min;
    result += this.CHAR_MIN;
    result += this.CHAR_SEP;
    result += sec.toFixed(2);
    result += this.CHAR_SEC;

    return result;

  }

  deg2dec(value: string) {

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

  decode(value: string): RegExpMatchArray {
    let pattern = '';

    // deg
    pattern += '(-?\\d+)';
    pattern += this.CHAR_DEG;
    pattern += '\\s*';

    // min
    pattern += '(\\d+)';
    pattern += this.CHAR_MIN;
    pattern += '\\s*';

    // sec
    pattern += '(\\d+(?:\\.\\d+)?)';
    pattern += this.CHAR_SEC;

    return value.match(new RegExp(pattern));
  }

  get latLonDeg(): string {
    return this.latDeg + ' N ' + this.lonDeg + ' E';
  }

  get gmapsURL(): string {
    return `https://www.google.com/maps/search/?api=1&query=${this.latDec},${this.lonDec}`;
  }

}
