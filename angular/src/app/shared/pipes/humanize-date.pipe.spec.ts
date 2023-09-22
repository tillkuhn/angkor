import { HumanizeDatePipe } from './humanize-date.pipe';
import {addDays} from 'date-fns';

describe('HumanizeDatePipe', () => {
  let pipe: HumanizeDatePipe;

  beforeEach(() => {
      pipe = new HumanizeDatePipe();
    }
  );

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
    const now = new Date()
    let dat : Date = now
    expect(pipe.transform(dat)).toContain('seconds');
    expect(pipe.transform(dat, {addSuffix: true})).toContain('ago');
    dat = addDays(now,2)
    expect(pipe.transform(dat)).toContain('2 days');
    dat = addDays(now,31)
    expect(pipe.transform(dat, {addSuffix: true})).toContain('in a month');
    dat = addDays(now,365)
    expect(pipe.transform(dat, {addSuffix: true})).toContain('in a year');
    dat = addDays(now,-35)
    expect(pipe.transform(dat, {addSuffix: true})).toContain('a month ago');
    dat = addDays(now,-400)
    expect(pipe.transform(dat, {addSuffix: true})).toContain('a year ago');
  });

  it('create handle undefined gracefully', () => {
    expect(pipe.transform(undefined)).toEqual('');
    expect(pipe.transform(null)).toEqual('');
  });
});
