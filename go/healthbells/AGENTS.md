# AGENTS.md – healthbells

Single-file Go service (`main.go`) that periodically checks URLs and serves health/status endpoints.

## Commands
- Tests: `go test -v ./...`, single test: `go test -run TestHealthCheckHandler -v`
- **`make test` is NOT unit tests**: it curls a running local server.
- `make format` runs goimports. Run it before committing.
- `make run` / `make run-alert` set `AWS_PROFILE=timafe` and refuse to start if `AWS_SESSION_TOKEN` is already set (open a fresh shell).

## Conventions
- Config comes only from env vars via envconfig with the `HEALTHBELLS_` prefix (see the `Config` struct; use `split_words:"true"` for multi-word fields).
- Uses the stdlib `log` logger with an app prefix (no zerolog here).
- Handler tests use `httptest` (see `main_test.go`).
