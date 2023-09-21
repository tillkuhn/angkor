# Angular 8 Tutorial: Learn to Build Angular 8 CRUD Web App

This source code is part of [Angular 8 Tutorial: Learn to Build Angular 8 CRUD Web App](https://www.djamware.com/post/5d0eda6f80aca754f7a9d1f5/angular-8-tutorial-learn-to-build-angular-8-crud-web-app)

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 7.3.3.

## Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

## Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

## Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

## Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

## Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

## Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).

## Update Angular 16 Diary

* Jest ^29 -> OK (but "ts-jest[versions] (WARN) Version 29.7.0 of jest installed has not been tested with ts-jest. If you're experiencing issues, consider using a supported version (>=28.0.0 <29.0.0-0). Please do not report issues in ts-jest if you are using unsupported versions" but ts-jest 29.1 is latest)
* jest-preset-angular 12 -> 13 also no problem but
```
* $ npx jest
  Determining test suites to run...
  ngcc-jest-processor: running ngcc
  Processing legacy "View Engine" libraries:
- ngx-web-storage [main/commonjs] (git+https://github.com/tim-kuteev/ngx-web-storage.git)
- Encourage the library authors to publish an Ivy distribution.
```



## Refactor crud components for different entities

### refactor components
```shell script
for ext in ts html scss; find src/app -name "*.$ext" -exec rename -v 's/product/place/' {} \;; done
for ext in ts html; do find src/app -name "*.$ext" -exec gsed -i 's/prod_price/price/g' {} \;; done
```
