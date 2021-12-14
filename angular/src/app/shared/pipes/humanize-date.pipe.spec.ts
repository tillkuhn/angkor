import { HumanizeDatePipe } from './humanize-date.pipe';

describe('HumanizeDatePipe', () => {
  let pipe: HumanizeDatePipe;

  beforeEach(() => {
      pipe = new HumanizeDatePipe();
    }
  );

  it('should create an instance', () => {
    expect(pipe).toBeTruthy();
    const dat = new Date()
    expect(pipe.transform(dat)).toContain('seconds');
    expect(pipe.transform(dat, {addSuffix: true})).toContain('ago');
  });

  it('create handle undefined gracefully', () => {
    expect(pipe.transform(undefined)).toEqual('');
    expect(pipe.transform(null)).toEqual('');
  });
});
