// See also https://github.com/thymikee/jest-preset-angular#getting-started
// related: <rootDir>/jest-test-setup.ts' (registered in setupFilesAfterEnv)
// also Adjust your tsconfig.spec.json with compiled options "types": ["jest"]

const { pathsToModuleNameMapper } = require('ts-jest');
const { compilerOptions } = require('./tsconfig');

// preset and setupFilesAfterEnv documented in https://www.npmjs.com/package/jest-preset-angular
// (the former globalSetup 'jest-preset-angular/global-setup' was only needed for ngcc and is gone since jest-preset-angular 15)
//
// To have some of your "node_modules" files transformed, specify a custom "transformIgnorePatterns" in your config.
// You can add support for multiple packages at once by separating them with a |: "node_modules/(?!module1|module2|etc)"
// https://github.com/ai/nanoid/issues/363
module.exports = {
  preset: 'jest-preset-angular',
  roots: ['<rootDir>/src/'],
  testMatch: ['**/+(*.)+(spec).+(ts)'],
  setupFilesAfterEnv: ['<rootDir>/jest-test-setup.ts'],
  collectCoverage: true,
  coverageReporters: ['html','lcovonly'],
  coverageDirectory:  '<rootDir>/coverage/',
  moduleNameMapper: pathsToModuleNameMapper(compilerOptions.paths || {}, {
    prefix: '<rootDir>/'
  }),
  // transform ESM-only packages (marked 16+), default of jest-preset-angular only covers *.mjs
  transformIgnorePatterns: ['node_modules/(?!.*\\.mjs$|marked/)']
  // transformIgnorePatterns: [`/node_modules/(?!nanoid)`]
};
