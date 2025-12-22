# Agent Guidelines for healthbells

## Build & Test Commands
- `make run` - Run main app with dev environment
- `make test` - Test with curl (manual testing)
- `go test -v ./...` - Run all tests
- `go test -run TestHealthCheckHandler -v` - Run single test
- `go test -run TestSpecificFunction ./...` - Run specific test across packages
- `make format` - Format code with goimports
- `make lint` - Run linting (if available)

## Code Style Guidelines
- **Imports**: Group stdlib, third-party, and local imports with blank lines
- **Formatting**: Use `goimports` for import organization and formatting
- **Naming**: CamelCase for exported, camelCase for unexported
- **Types**: Use explicit struct field tags for configuration
- **Error Handling**: Check errors immediately, use logger.Printf for non-fatal errors
- **Concurrency**: Use sync.Mutex for shared state, channels for communication
- **HTTP Handlers**: Return JSON for APIs, HTML for human-facing endpoints
- **Configuration**: Use envconfig with struct tags, prefix with app name
- **Logging**: Use structured logging with app prefix, avoid emojis in production logs