import {BytesizePipe} from './bytesize.pipe';

// test filesize formatter
describe('BytesizePipe', () => {

  let pipe: BytesizePipe;

  beforeEach(() => {
      pipe = new BytesizePipe();
    }
  );

  it('create an instance', () => {
    expect(pipe).toBeTruthy();
    const gb = pipe.transform(1551859712);
    expect(gb).toBe('1.6 GB');
    expect(pipe.transform(5344000)).toEqual('5.3 MB');
    expect(pipe.transform(55500)).toEqual('55.5 kB');
  });

  it('create handle undefined gracefully', () => {
    expect(pipe.transform(undefined)).toEqual(undefined);
  });

});
