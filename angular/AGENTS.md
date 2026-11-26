# AGENTS.md - Development Guidelines for Angkor UI

## Toolchain
- Angular 22 with the esbuild/Vite based `@angular/build:application` builder, output in `dist/webapp` (no `browser/` subdir, see `outputPath.browser` in `angular.json`)
- Node `^22.22.3 || ^24.15.0` (see `.nvmrc`, `nvm use`), yarn 1
- ESLint flat config in `eslint.config.js` (angular-eslint + typescript-eslint)
- Jest 30 + jest-preset-angular (not Karma/Vitest); ESM-only packages like `marked` must be listed in `transformIgnorePatterns` in `jest.config.js`
- mapbox-gl loads its prebuilt worker from `assets/mapbox` (copied by `angular.json`), because Angular's transpilation breaks the bundled worker
- Material uses the M2 theme (`mat.m2-*`) plus global MDC tweaks in `src/styles.scss` (lists, cards, form fields, button rows)

## Build Commands
- `yarn test` - Run all Jest tests
- `yarn test -- --testNamePattern="ComponentName"` - Run single test by pattern
- `yarn test:coverage` - Run tests with coverage
- `yarn lint` - Run ESLint
- `yarn lint:fix` - Auto-fix ESLint issues
- `yarn build` - Development build
- `yarn build:prod` - Production build
- `make lint` - Alternative lint command with Dockerfile linting

## Code Style Guidelines

### Imports & Path Aliases
- Use path aliases: `@app/*`, `@shared/*`, `@domain/*`
- Group imports: Angular libs, third-party, local modules
- Example: `import {Component} from '@angular/core'; import {Dish} from '@app/domain/dish';`

### TypeScript & Types
- Use interfaces for domain models (see `src/app/domain/`)
- Prefer explicit types over `any`
- Use generics for reusable components
- Declare custom types with `declare type` when needed

### Naming Conventions
- Components: PascalCase with `app` prefix (e.g., `app-dishes`)
- Services: PascalCase ending with `Service` (e.g., `AuthService`)
- Variables: camelCase
- Constants: UPPER_SNAKE_CASE
- Files: kebab-case for components, camelCase for services

### Error Handling
- Use RxJS operators for async error handling
- Log errors with NGXLogger
- Implement proper error states in UI
- Use try-catch for synchronous operations

### Angular Patterns
- Use `providedIn: 'root'` for services
- Prefer reactive patterns with Subjects/Observables
- Use `async` pipe in templates
- Implement `OnDestroy` for cleanup

### Testing
- Use Jest with `jest-preset-angular`
- Test files: `*.spec.ts`
- Mock services in tests
- Test both happy path and error cases