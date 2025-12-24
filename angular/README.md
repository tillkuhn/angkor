# Angkor UI based on Angular framework

## Overview
Angkor UI is a comprehensive Angular web application for managing and exploring location-based content. It serves as a content management system for places, dishes, notes, photos, videos, tours, and other geographic entities.

## Key Features

### Core Entity Management
- **Places**: Add, edit, and view geographic locations with metadata
- **Dishes**: Culinary content management with photos and details
- **Notes**: Personal notes with speech-to-text capabilities
- **Media**: Photo, video, and tour management with geolocation
- **Areas**: Hierarchical geographic organization
- **Events**: Event tracking and management

### Interactive Features
- **Map Integration**: Mapbox-powered interactive mapping with geocoder
- **Location Search**: Advanced location-based search functionality
- **Tag Cloud**: Visual tag exploration
- **Feeds**: RSS/feed aggregation and display
- **Radio**: Audio streaming component
- **User Profiles**: User management and content association

### Technical Stack
- **Framework**: Angular 16 with TypeScript 4.9+
- **UI Library**: Angular Material 16 with CDK
- **Mapping**: Mapbox GL with ngx-mapbox-gl
- **Testing**: Jest with jest-preset-angular
- **Build**: Angular CLI with production optimizations
- **Mock Server**: JSON Server for development data

## Architecture

### Domain Models
Located in `src/app/domain/` with TypeScript interfaces:
- Area, Dish, Event, Link, Location, Note, Place, POI, Tag, User

### Feature Modules
- `areas/` - Geographic area management
- `dishes/` - Culinary content CRUD
- `places/` - Location management
- `notes/` - Note-taking with speech
- `locatables/` - Location-based media (photos, videos, tours, posts)
- `map/` - Interactive mapping
- `shared/` - Reusable components and services

### Services
- Store services for state management
- Authentication and authorization
- File upload and media handling
- Geolocation and mapping services
- Logging and error handling

## Development

### Prerequisites
- Node.js 18.13+ or 20.0+
- Yarn package manager
- Angular CLI 16.2+

### Quick Start
```bash
yarn install
yarn start          # Development server
yarn mock           # Mock API server (port 8080)
```

### Available Scripts
- `yarn test` - Run Jest test suite
- `yarn test:coverage` - Run tests with coverage
- `yarn lint` - ESLint code analysis
- `yarn build:prod` - Production build
- `yarn mock:delay` - Mock server with 3s delay

### Testing
- Unit tests with Jest
- Component testing
- Coverage reporting
- E2E tests with Protractor

## Configuration
- Environment-specific configs in `environments/`
- Proxy configuration for API calls
- Docker support with included Dockerfile
- Nginx configuration for production deployment

## Notable Integrations
- YouTube Player for video content
- Markdown support for rich text
- File upload with Imagine module
- Rating system
- Tag management
- Speech-to-text for notes
- RSS feed processing

This project demonstrates a mature Angular application with comprehensive CRUD operations, real-time features, and modern development practices.

## Legacy Notes

### Refactor crud components for different entities

```shell script
for ext in ts html scss; find src/app -name "*.$ext" -exec rename -v 's/product/place/' {} \;; done
for ext in ts html; do find src/app -name "*.$ext" -exec gsed -i 's/prod_price/price/g' {} \;; done
```


This source code is part of [Angular 8 Tutorial: Learn to Build Angular 8 CRUD Web App](https://www.djamware.com/post/5d0eda6f80aca754f7a9d1f5/angular-8-tutorial-learn-to-build-angular-8-crud-web-app)

This project was generated with [Angular CLI](https://github.com/angular/angular-cli) version 7.3.3.

### Development server

Run `ng serve` for a dev server. Navigate to `http://localhost:4200/`. The app will automatically reload if you change any of the source files.

### Code scaffolding

Run `ng generate component component-name` to generate a new component. You can also use `ng generate directive|pipe|service|class|guard|interface|enum|module`.

### Build

Run `ng build` to build the project. The build artifacts will be stored in the `dist/` directory. Use the `--prod` flag for a production build.

### Running unit tests

Run `ng test` to execute the unit tests via [Karma](https://karma-runner.github.io).

### Running end-to-end tests

Run `ng e2e` to execute the end-to-end tests via [Protractor](http://www.protractortest.org/).

### Further help

To get more help on the Angular CLI use `ng help` or go check out the [Angular CLI README](https://github.com/angular/angular-cli/blob/master/README.md).

### Update Angular 17 Diary

* Check https://angular.dev/update-guide?v=16.0-17.0&l=2
* Angular v17 supports Zone.js version 0.14.x or later.

```
nvm install v22.21.0 && nvm use v22.21.0
ng update @angular/core@17 @angular/cli@17
The installed Angular CLI version is outdated.
Installing a temporary Angular CLI versioned 17.3.17 to perform the update.
                 Package "@angular-eslint/schematics" has an incompatible peer dependency to "@angular/cli" (requires ">= 16.0.0 < 17.0.0", would install "17.3.17").
✖
```

### Update Angular 16 Diary

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
* zone.js must be <14 for angular 15 @angular/core@15.2.9" has incorrect peer dependency "zone.js@~0.11.4 || ~0.12.0 || ~0.13.0"
* https://angular.io/guide/update-to-version-16 and https://update.angular.io/?v=15.0-16.0
* ake sure that you are using a supported version of TypeScript before you upgrade your application. Angular v16 supports TypeScript version 4.9.3 or later.
* Make sure that you are using a supported version of Zone.js before you upgrade your application. Angular v16 supports Zone.js version 0.13.x or later.
* Due to the removal of the Angular Compatibility Compiler (ngcc) in v16, projects on v16 and later no longer support View Engine libraries.

```
ng update --allow-dirty @angular/cdk  @angular/core  @angular/material @angular/youtube-player  @angular/cli @angular-eslint/schematics
```
