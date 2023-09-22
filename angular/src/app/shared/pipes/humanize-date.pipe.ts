import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'humanizeDate'
})
export class HumanizeDatePipe implements PipeTransform {

  format(str: string, future: boolean, addInAgo: boolean): string {
    // check if time is in the future (prefix "in") or in the past (suffix "ago")
    if (addInAgo) {
      return future ? `in ${str}` : `${str} ago`;
    } else {
      return str;
    }
  }

  transform(
    value: Date | null | undefined,
    options?: {
      includeSeconds?: boolean;
      addSuffix?: boolean;
    }
  ): string {

    if (value === null || value === undefined) {
      return ''; // return to sender
    }

    const now = new Date();
    const seconds = Math.round(Math.abs((now.getTime() - value.getTime()) / 1000));
    const minutes = Math.round(Math.abs(seconds / 60));
    const hours = Math.round(Math.abs(minutes / 60));
    const days = Math.round(Math.abs(hours / 24));
    const months = Math.round(Math.abs(days / 30.416));
    const years = Math.round(Math.abs(days / 365));
    const addInAgo = options?.addSuffix;
    const future = !Number.isNaN(seconds) && value.getTime() > now.getTime();
    if (Number.isNaN(seconds)) {
      return 'NaN: ' + value;
    } else if (seconds <= 45) {
      return this.format('a few seconds', future, addInAgo);
    } else if (seconds <= 90) {
      return this.format('a minute', future, addInAgo);
    } else if (minutes <= 45) {
      return this.format(minutes + ' minutes', future, addInAgo);
    } else if (minutes <= 90) {
      return this.format('an hour', future, addInAgo);
    } else if (hours <= 22) {
      return this.format(hours + ' hours', future, addInAgo);
    } else if (hours <= 36) {
      return this.format('a day', future, addInAgo);
    } else if (days <= 25) {
      return this.format(days + ' days', future, addInAgo);
    } else if (days <= 45) {
      return this.format('a month', future, addInAgo);
    } else if (days <= 345) {
      return this.format(months + ' months', future, addInAgo);
    } else if (days <= 545) {
      return this.format('a year', future, addInAgo);
    } else { // (days > 545)
      return this.format(years + ' years', future, addInAgo);
    }
  }

}
