# AGENTS.md - Development Guidelines for Angkor UI

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