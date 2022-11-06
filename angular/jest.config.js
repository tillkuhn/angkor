const { pathsToModuleNameMapper } = require('ts-jest');
const { compilerOptions } = require('./tsconfig');

// preset, setupFilesAfterEnv and globalSetup documented in https://www.npmjs.com/package/jest-preset-angular
// Angular 14: globalSetup is super important, or you constructor / Inject warnings  such as
// NG0202: This constructor is not compatible with Angular Dependency Injection, see also
// https://github.com/angular/angular/issues/45021
//
// To have some of your "node_modules" files transformed, specify a custom "transformIgnorePatterns" in your config.
// You can add support for multiple packages at once by separating them with a |: "node_modules/(?!module1|module2|etc)"
// https://github.com/ai/nanoid/issues/363
module.exports = {
  preset: 'jest-preset-angular',
  roots: ['<rootDir>/src/'],
  testMatch: ['**/+(*.)+(spec).+(ts)'],
  setupFilesAfterEnv: ['<rootDir>/jest-test-setup.ts'],
  globalSetup: 'jest-preset-angular/global-setup',
  collectCoverage: true,
  coverageReporters: ['html','lcovonly'],
  coverageDirectory:  '<rootDir>/coverage/',
  moduleNameMapper: pathsToModuleNameMapper(compilerOptions.paths || {}, {
    prefix: '<rootDir>/src'
  })
  // transformIgnorePatterns: [`/node_modules/(?!nanoid)`]
};
