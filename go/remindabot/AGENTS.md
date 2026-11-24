# AGENTS.md – remindabot

## Commands
- `make test` or `go test -v .`; single test: `go test -run TestFunctionName`
- `make lint` runs `make format` (goimports + go fmt), then `golangci-lint run`.
- `make dryrun` (alias `dry`): sets `REMINDABOT_SMTP_DRYRUN=true` and debug logging. `make run` and `runext` don't set dry run, so they may send real mails.
- The Makefile reads tokens from `~/.angkor/.env`.

## Conventions
- Config: env vars with the `REMINDABOT_` prefix via envconfig (`config.go`, `split_words`).
- Logging: zerolog, `log.With().Str("app", AppId)`.
- Tests use testify/assert.
