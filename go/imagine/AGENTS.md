# AGENTS.md – imagine

S3-backed image/audio upload and serving service. `main.go` wires the config, AWS session, upload worker queue and HTTP server. Packages: `server` (gorilla/mux handlers), `s3` (worker), `image`, `audio`, `auth`, `types`, `utils`.

## Commands
- `make test`: uses `gotest` if installed, else `go test`, with coverage into `coverage.out`/`coverage.html` (gitignored).
- Single test: `go test ./server -run TestName -count=1`
- `make lint` runs `make format` (go fmt + goimports), then `golangci-lint run`.
- `make build` → `dist/imagine`; `make build-linux` cross-compiles with `CGO_ENABLED=0`.
- `make run` reads secrets from `~/.angkor/.env`, uses `AWS_PROFILE=timafe`, and refuses to start if `AWS_SESSION_TOKEN` is set. Listens on port 8090.
- The `make get-*` / `make post-*` targets are curl fixtures for the running server.

## Conventions
- Config: env vars with the `IMAGINE_` prefix, defined in `types/config.go`. New fields need `default`, `desc` (and `split_words` if needed) tags. Document them in `README.adoc`.
- Uses AWS SDK **v1** (`aws-sdk-go`), not v2.
- Logging: zerolog with component loggers via `log.Logger.With().Str("logger", "<component>")`.
- Close files/bodies with `defer utils.CheckedClose(...)`.
- Auth middleware accepts `X-Authorization` and falls back to `Authorization`. Wrap new protected routes with `ValidationMiddleware`.
- Tag maps are encoded sorted (`encodeTagMap`). Keep that deterministic.
- Temporary uploads go to `./upload` (`Dumpdir`) and must be removed after the S3 upload.
