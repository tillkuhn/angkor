# HealthBells - Simple Golang Healthcheck

* Async stuff based on [Asynchronous programming with Go](https://medium.com/@gauravsingharoy/asynchronous-programming-with-go-546b96cd50c1)
* Uses hightowers [envkonfig](https://github.com/kelseyhightower/envconfig) for config

# Init
```
go mod init github.com/tillkuhn/angkor/tools/healthbells
```
# Run 
```
HEALTHBELLS_DEBUG=true HEALTHBELLS_INTERVALSECONDS=10 go run main.go
```
# Go sandbox

Run from a directory of your project to download all go-gettable dependencies.
```
go get -d ./...
```

The second will list all subdependencies, the first only the directly imported packages. [source](https://stackoverflow.com/questions/32758235/how-to-get-all-dependency-files-for-a-program-using-golang)
```
go list -f '{{ join .Imports "\n" }}'
go list -f '{{ join .Deps "\n" }}'
```
https://golang.cafe/blog/upgrade-dependencies-golang.html
