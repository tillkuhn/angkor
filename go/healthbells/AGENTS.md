# Agent Guidelines for healthbells

This document provides coding agents with essential information about the healthbells project - a lightweight Go service for monitoring website availability through periodic health checks.

## Project Overview

- **Language**: Go 1.23+ (currently using Go 1.24.2)
- **Module**: `github.com/tillkuhn/angkor/tools/healthbells`
- **Purpose**: URL health monitoring service with HTTP status endpoints
- **Dependencies**: envconfig (config), go-humanize (time formatting)

## Build & Test Commands

### Running the Application
```bash
make run              # Run with dev environment (requires AWS profile)
make run-alert        # Run with intentionally failing URL for testing alerts
go run main.go        # Direct run (configure via environment variables)
```

### Testing
```bash
go test -v ./...                           # Run all tests with verbose output
go test -run TestHealthCheckHandler -v    # Run specific test by name
go test -run TestSpecificFunction ./...   # Run named test across all packages
go test -cover ./...                      # Run tests with coverage report
make test                                 # Manual testing via curl (requires running server)
```

### Code Quality
```bash
make format           # Format code with goimports (run before committing)
goimports -w .        # Alternative: format all files directly
go vet ./...          # Run Go's built-in code analyzer
golangci-lint run     # Run comprehensive linter (if golangci-lint installed)
go mod tidy           # Clean up module dependencies
```

### Dependency Management
```bash
make outdated         # Show outdated direct dependencies
make update           # Update all dependencies (go get -u all)
go get -u package     # Update specific package
```

## Code Style Guidelines

### Project Structure
```
healthbells/
├── main.go           # Main application with HTTP handlers and health checking logic
├── main_test.go      # HTTP handler tests using httptest
├── go.mod            # Go module definition
├── Makefile          # Build automation
└── AGENTS.md         # This file
```

### Import Organization
Always group imports into three sections with blank lines between them:

```go
import (
    // 1. Standard library imports
    "context"
    "fmt"
    "net/http"
    
    // 2. Third-party imports
    "github.com/dustin/go-humanize"
    "github.com/kelseyhightower/envconfig"
    
    // 3. Local/project imports (if any)
    // "github.com/tillkuhn/angkor/go/topkapi"
)
```

**Tool**: Use `goimports` (not just `gofmt`) for automatic import organization and formatting.

### Naming Conventions
- **Exported** (public): `CamelCase` - e.g., `CheckResult`, `HealthStatus`, `AppId`
- **Unexported** (private): `camelCase` - e.g., `checkUrl`, `quitChannel`, `logger`
- **Constants**: `CamelCase` for exported, `camelCase` for unexported
- **Acronyms**: Keep consistent case - `HTTP`, `URL`, `AppId` (not `AppID` unless entire name)

### Type Definitions

#### Configuration Structs
Use explicit struct field tags for environment-based configuration with envconfig:
```go
type Config struct {
    Quiet    bool          `default:"true"`              // Simple default
    Port     int           `default:"8091"`              // Numeric default
    Interval time.Duration `default:"5s"`                // Duration parsing
    Urls     []string      `default:"https://example.com/"` // Array parsing
    KafkaSupport bool `default:"true" split_words:"true"` // KAFKA_SUPPORT env var
}
```

**Pattern**: Use `split_words:"true"` to map `CamelCase` to `SNAKE_CASE` environment variables.

#### Data Structures
Keep struct fields unexported unless they need to be serialized (JSON/XML):
```go
type CheckResult struct {
    target       string        // unexported - internal use only
    healthy      bool
    responseTime time.Duration
    statusCode   int
}

// For JSON responses, export fields:
type APIResponse struct {
    Status string `json:"status"`
    Time   string `json:"time"`
}
```

### Error Handling

**Critical errors** (fatal): Use `logger.Fatal()` to log and exit
```go
if err != nil {
    logger.Fatal(err.Error())
}
```

**Non-fatal errors**: Log with `logger.Printf()` and continue execution
```go
if err != nil {
    logger.Printf("Error creating request for %s: %v", url, err)
    return
}
```

**HTTP handler errors**: Use `http.Error()` with appropriate status codes
```go
if err != nil {
    http.Error(w, err.Error(), http.StatusInternalServerError)
    return
}
```

### Concurrency Patterns

#### Goroutines with Channels
Use buffered channels for known concurrent operations:
```go
resultChannel := make(chan CheckResult, len(urls))
for _, url := range urls {
    go checkUrl(url, resultChannel)
}
for i := 0; i < len(urls); i++ {
    result := <-resultChannel
    // process result
}
```

#### Mutex for Shared State
Use embedded mutex in structs requiring synchronization:
```go
type HealthStatus struct {
    *sync.Mutex  // inherits Lock() and Unlock() methods
    Results []CheckResult
}

// Usage:
healthStatus.Lock()
defer healthStatus.Unlock()
// ... modify healthStatus.Results ...
```

#### Signal Channels
Use `sync.Once` for idempotent channel closing:
```go
var quitOnce sync.Once
quitOnce.Do(func() {
    close(quitChannel)
})
```

### HTTP Handlers

**Pattern**: Handler signatures follow `func(w http.ResponseWriter, r *http.Request)`

**JSON APIs**: Set Content-Type and use json.Marshal
```go
func health(w http.ResponseWriter, _ *http.Request) {
    w.Header().Set("Content-Type", "application/json")
    status, err := json.Marshal(map[string]any{
        "status": "up",
        "info":   fmt.Sprintf("%s is healthy", AppId),
    })
    // ... error handling and write
}
```

**HTML Endpoints**: Return HTML for human-facing pages
```go
func status(w http.ResponseWriter, _ *http.Request) {
    w.Header().Set("Content-Type", "text/html; charset=utf-8")
    fmt.Fprintf(w, `<html>...</html>`)
}
```

### Context Usage
Always use context for operations with timeouts or cancellation:
```go
ctx, cancel := context.WithTimeout(context.Background(), config.Timeout)
defer cancel()
req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
```

### Logging

**Logger initialization**: Create package-level logger with app prefix
```go
logger = log.New(os.Stdout, fmt.Sprintf("[%-10s] ", AppId), log.LstdFlags)
```

**Production logs**: Avoid emojis in production; use them sparingly for development visibility
```go
// Development/interactive mode
logger.Printf("💖 %s is up with status=%d", url, statusCode)

// Better for production
logger.Printf("Service %s is healthy (status=%d)", url, statusCode)
```

## Testing Guidelines

### HTTP Handler Tests
Use `httptest` package for testing HTTP handlers:
```go
func TestHealthCheckHandler(t *testing.T) {
    req, err := http.NewRequest("GET", "/health", nil)
    if err != nil {
        t.Fatal(err)
    }
    
    rr := httptest.NewRecorder()
    handler := http.HandlerFunc(health)
    handler.ServeHTTP(rr, req)
    
    if status := rr.Code; status != http.StatusOK {
        t.Errorf("got %v want %v", status, http.StatusOK)
    }
}
```

### Test Organization
- Place tests in `*_test.go` files in the same package
- Use table-driven tests for multiple scenarios
- Test both success and error paths
- Use `t.Fatal()` for setup errors, `t.Error()` for assertion failures

## Environment Configuration

Configure the application via environment variables with `HEALTHBELLS_` prefix:

```bash
HEALTHBELLS_QUIET=false      # Enable verbose logging
HEALTHBELLS_PORT=8091        # HTTP server port
HEALTHBELLS_INTERVAL=5s      # Check interval (use -1ms to disable periodic checks)
HEALTHBELLS_TIMEOUT=10s      # HTTP request timeout
HEALTHBELLS_URLS=url1,url2   # Comma-separated list of URLs to monitor
HEALTHBELLS_HISTLEN=25       # Number of historical results to keep
HEALTHBELLS_KAFKA_SUPPORT=true # Enable Kafka event publishing
```

## Common Patterns in This Codebase

1. **Circular Buffer**: Use modulo arithmetic for efficient fixed-size history
2. **Graceful Shutdown**: Use quit channels with `sync.Once` for idempotent cleanup
3. **Async URL Checking**: Spawn goroutines for parallel checks, collect via channels
4. **Structured Configuration**: Single `Config` struct with envconfig tags
5. **Package-level State**: Use global vars for singleton resources (logger, config)

## Before Committing

1. Run `make format` or `goimports -w .`
2. Run `go test -v ./...` and ensure all tests pass
3. Run `go vet ./...` to catch common issues
4. Run `go mod tidy` to clean up dependencies
5. Ensure no sensitive data (passwords, tokens) in code

## Additional Resources

- [Envconfig Documentation](https://github.com/kelseyhightower/envconfig)
- [Go Humanize Library](https://github.com/dustin/go-humanize)
- [Effective Go](https://go.dev/doc/effective_go)
- [Go Code Review Comments](https://github.com/golang/go/wiki/CodeReviewComments)
