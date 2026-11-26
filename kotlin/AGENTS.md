# AGENTS.md

## Build / Test Commands

- **Build**: `./gradlew build`
- **Test**: `./gradlew test` (integration tests need local Postgres `angkor_test`)
- **Unit tests only (no DB)**: `./gradlew test --tests "*UT"`
- **Single test**: `./gradlew test --tests "ClassName.testMethodName"` (e.g., `./gradlew test --tests "AreaServiceUT.countriesAndRegions"` )
- **Test with debug**: `./gradlew test --debug-jvm` (listens on port 5005)
- **Coverage report**: `./gradlew test` generates report at `build/reports/jacoco/test/html/index.html`

## Code Style Guidelines

### Language & Framework
- Kotlin + Spring Boot. Versions live in `gradle/libs.versions.toml`; the JVM target is set in `build.gradle.kts` (`sourceCompatibility` and `jvmTarget` must match).
- JPA entities use `data class` with Hibernate + Hypersistence Utils
- Tests use JUnit 5, Mockito Inline (mocks final classes), WireMock

### Imports & Formatting
- Use wildcard imports sparingly; prefer explicit imports
- Group imports: stdlib, third-party, project (separated by blank line)
- Use `const val` for compile-time constants; shared constants in `Constants.kt` object

### Naming Conventions
- Classes: PascalCase (e.g., `AreaService`, `AreaController`)
- Functions: camelCase (e.g., `findAll()`, `countriesAndRegions()`)
- Properties: camelCase, immutable by default (`val`)
- Test methods: backtick names describing behavior (e.g., `it should list all actions`)

### Types & Nullability
- Prefer non-nullable types; nullable only when needed (`Type?`)
- Repository IDs are non-nullable (`ID`), use `!!` for null checks if compiler validates it
- Generic bounds: `ET: Any` to enforce non-nullable type parameters

### Error Handling
- Log meaningful messages with `logPrefix()` helper in services
- Throw `UnsupportedOperationException` for unimplemented repo interfaces
- Test logs show full exception stack traces by default

### Architecture
- Services extend `AbstractEntityService<ET, EST, ID>` and implement `entityType()`
- Controllers extend `AbstractEntityController<ET, EST, ID>`
- Repositories implement `Searchable<*>` for search, `AuthScopeSupport<*>` for auth filtering
- Use `@Transactional(readOnly = true)` for read operations
- Avoid `@Suppress("UNCHECKED_CAST")` unless necessary; add `todo do better`

### Security & Auth
- OAuth2 login + Basic Auth for specific endpoints (e.g., `/actuator/prometheus`)
- Local UI testing without Cognito: `APP_NOAUTH_USER_ID=<app_user id> make run-noauth` (profile `noauth`, never active with `prod`) authenticates every request as that user with `ROLE_USER` + `ROLE_ADMIN`, see `NoAuthFilter`
- Role-based access: `ROLE_USER`, `ROLE_ADMIN` (prefix added automatically)
- Auth scopes filtered via `SecurityUtils.allowedAuthScopes()`
- Service accounts: see `ServiceAccountToken.kt`

### Comments
- KDoc for public APIs (`/** ... */`)
- Keep implementation comments minimal; code should be self-documenting