// see jest.config.js, this setup script is launched in line
//  setupFilesAfterEnv: ['<rootDir>/jest-test-setup.ts'],
// See also https://github.com/thymikee/jest-preset-angular#getting-started
import 'jest-preset-angular/setup-jest';

// Additional Hacks go here ...
// See https://stackoverflow.com/a/52969731/4292075 + https://github.com/mapbox/mapbox-gl-js/issues/9889
function noOp() {
  // This is intentional to avoid issue with mapbox and Jest URL.createObjectURL is not a function
}
if (typeof window.URL.createObjectURL === 'undefined') {
  Object.defineProperty(window.URL, 'createObjectURL', { value: noOp})
}

Object.defineProperty(window, 'CSS', {value: null});
Object.defineProperty(window, 'getComputedStyle', {
  value: () => {
    return {
      display: 'none',
      appearance: ['-webkit-appearance']
    };
  }
});

Object.defineProperty(document, 'doctype', {
  value: '<!DOCTYPE html>'
});

Object.defineProperty(document.body.style, 'transform', {
  value: () => {
    return {
      enumerable: true,
      configurable: true
    };
  }
});
