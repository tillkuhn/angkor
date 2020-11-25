import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'bytesize'
})
export class BytesizePipe implements PipeTransform {

  // todo support si and dp in caller
  transform(bytes: number, ...args: unknown[]): string | number {
    if (bytes === null || bytes === undefined || Number.isNaN(bytes)) {
      return bytes; // return to sender
    }
    return this.humanFileSize(bytes);
  }

  humanFileSize(bytes: number, si = true, dp = 1): string {
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

}
