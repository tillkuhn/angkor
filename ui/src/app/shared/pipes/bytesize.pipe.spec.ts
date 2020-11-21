import {BytesizePipe} from './bytesize.pipe';

// test filesize formatter
// console.log(Utils.humanFileSize(1551859712));  // 1.4 GiB
// console.log(Utils.humanFileSize(5000, true));  // 5.0 kB
// console.log(Utils.humanFileSize(5344000, true));  // 5.3 MB
// console.log(Utils.humanFileSize(5000, false));  // 4.9 KiB
// console.log(Utils.humanFileSize(undefined)); // ""

describe('BytesizePipe', () => {
  it('create an instance', () => {
    const pipe = new BytesizePipe();
    expect(pipe).toBeTruthy();
    const gb = pipe.transform(1551859712);
    expect(gb).toBe('1.6 GB');
    expect(pipe.transform(5344000)).toBe('5.3 MB');
  });
});
