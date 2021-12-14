import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'humanizeDate'
})
export class HumanizeDatePipe implements PipeTransform {

  transform(
      value: Date | null | undefined,
      options?: {
        includeSeconds?: boolean;
        addSuffix?: boolean;
      }
  ): string {
    if (value === null || value === undefined) {
      return ''; // return to sender
    }``
    const suffix = options?.addSuffix ? ' ago' : '';
    const now = new Date();
    const seconds = Math.round(Math.abs((now.getTime() - value.getTime()) / 1000));
    const minutes = Math.round(Math.abs(seconds / 60));
    const hours = Math.round(Math.abs(minutes / 60));
    const days = Math.round(Math.abs(hours / 24));
    const months = Math.round(Math.abs(days / 30.416));
    const years = Math.round(Math.abs(days / 365));
    if (Number.isNaN(seconds)) {
      return 'NaN: ' + value;
    } else if (seconds <= 45) {
      return `a few seconds${suffix}`;
    } else if (seconds <= 90) {
      return `a minute${suffix}`;
    } else if (minutes <= 45) {
      return minutes + ` minutes${suffix}`;
    } else if (minutes <= 90) {
      return `an hour${suffix}`;
    } else if (hours <= 22) {
      return hours + ` hours${suffix}`;
    } else if (hours <= 36) {
      return `a day${suffix}`;
    } else if (days <= 25) {
      return days + ` days${suffix}`;
    } else if (days <= 45) {
      return `a month${suffix}`;
    } else if (days <= 345) {
      return months + ` months${suffix}`;
    } else if (days <= 545) {
      return `a year${suffix}`;
    } else { // (days > 545)
      return years + ` years${suffix}`;
    }
  }

}
