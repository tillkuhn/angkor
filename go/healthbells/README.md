# HealthBells - Simple Golang Healthcheck

HealthBells is a lightweight Go service that monitors the availability of multiple URLs/websites through periodic health checks. It provides HTTP endpoints for status monitoring and maintains a configurable history of check results with response times and status codes.

# Inspired by ... 

* Async stuff based on [Asynchronous programming with Go](https://medium.com/@gauravsingharoy/asynchronous-programming-with-go-546b96cd50c1)
* Uses Hightowers [envconfig](https://github.com/kelseyhightower/envconfig) for config
* Pause and resume: [Is there some elegant way to pause & resume any other goroutine in golang?](https://stackoverflow.com/questions/16101409/is-there-some-elegant-way-to-pause-resume-any-other-goroutine-in-golang)
* [Sample HTTP Server with signal handling](https://github.com/kelseyhightower/helloworld/blob/master/main.go)
* [Getting Started With Golang Channels](https://medium.com/technofunnel/understanding-goroutine-go-channels-in-detail-9c5a28f08e0d)


# Init
```
go mod init github.com/tillkuhn/angkor/tools/healthbells
```
# Run 
```
HEALTHBELLS_DEBUG=true HEALTHBELLS_INTERVALS=10s go run main.go
```

# Other Build & Test Commands

- `make run` - Run main app with dev environment
- `make test` - Test with curl (manual testing)
- `go test -v ./...` - Run all tests
- `go test -run TestHealthCheckHandler -v` - Run single test
- `go test -run TestSpecificFunction ./...` - Run specific test across packages
- `make format` - Format code with goimports
- `make lint` - Run linting (if available)


# Check out ...

* [Humane Units to humanize times etc.](https://github.com/dustin/go-humanize)
* [Artificially create a connection timeout error](https://stackoverflow.com/questions/100841/artificially-create-a-connection-timeout-error) TL;TR: Use `http://example.com:81`
