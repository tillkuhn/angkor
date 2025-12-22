# AGENTS.md

## Build Commands
- `make format` or `make fmt` - Format code with goimports and go fmt
- `make lint` - Run golangci-lint (includes formatting)
- `make test` - Run all tests
- `go test -run TestFunctionName` - Run single test
- `go run *.go` - Run main application
- `make dryrun` - Dry run with debug logging

## Code Style Guidelines
- **Imports**: Group stdlib, third-party, and local imports with blank lines. Use goimports
- **Formatting**: Standard gofmt formatting with goimports
- **Naming**: CamelCase for exported, lowerCamelCase for unexported. Acronyms in ALLCAPS (e.g., API, URL, SMTP)
- **Types**: Use concrete structs over interfaces. Config struct uses envconfig tags with split_words
- **Error Handling**: Return errors explicitly, don't panic. Use log.Fatal for unrecoverable errors
- **Logging**: Use zerolog with structured logging. Logger includes app context: log.With().Str("app", AppId).Logger()
- **Comments**: Minimal comments, prefer self-documenting code
- **Testing**: Use testify/assert. Test functions start with Test, use table-driven tests for multiple cases
- **Environment**: All config via environment variables using envconfig with REMINDABOT_ prefix