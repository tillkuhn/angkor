import {SmartCoordinates} from '@shared/domain/smart-coordinates';

// Simple Unit Tests Coordinates Formatting
describe('SmartCoordinates', () => {

  /*
  // let coordinates: SmartCoordinates;
  beforeEach(() => {
      coordinates = new BytesizePipe();
    }
  );
   */

  it('should convert coordinate strings', () => {
    const coordinates = new SmartCoordinates('13.7248936,100.4930268');
    expect(coordinates).toBeTruthy();
    expect(coordinates.lonLatArray.length).toEqual(2);
    expect(coordinates.lonLatArray[0]).toEqual(100.4930268);
    expect(coordinates.lonLatArray[1]).toEqual(13.7248936);
  });

  it('should convert coordinate number arrays', () => {
    const coa = [100.4930268, 13.7248936]
    const coordinates = new SmartCoordinates(coa);
    expect(coordinates).toBeTruthy();
    for (const coordinate of coa) {
      expect(coordinates.gmapsUrl).toContain(coordinate.toString());
    }
  });

});
