/**
 * thank you
 * https://github.com/perfectline/geopoint
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

  constructor(lon: number, lat: number) {

    this.lonDeg = this.dec2deg(lon, this.MAX_LON);
    this.lonDec = lon;

    this.latDeg = this.dec2deg(lat, this.MAX_LAT);
    this.latDec = lat;
  }

  dec2deg(value, max): string {

    let sign = value < 0 ? -1 : 1;

    let abs = Math.abs(Math.round(value * 1000000));

    if (abs > (max * 1000000)) {
      return 'NaN';
    }

    let dec = abs % 1000000 / 1000000;
    let deg = Math.floor(abs / 1000000) * sign;
    let min = Math.floor(dec * 60);
    let sec = (dec - min / 60) * 3600;

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

  deg2dec(value) {

    let matches = this.decode(value);

    if (!matches) {
      return NaN;
    }

    let deg = parseFloat(matches[1]);
    let min = parseFloat(matches[2]);
    let sec = parseFloat(matches[3]);

    if (isNaN(deg) || isNaN(min) || isNaN(sec)) {
      return NaN;
    }

    return deg + (min / 60.0) + (sec / 3600);
  }

  decode(value) {
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

  getLonDec() {
    return this.lonDec;
  }

  getLatDec() {
    return this.latDec;
  }

  getLonDeg() {
    return this.lonDeg;
  }

  getLatDeg() {
    return this.latDeg;
  }

  getLatLonDeg() {
    return this.latDeg + ' N ' + this.lonDeg + ' E';
  }

}
