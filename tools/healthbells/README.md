# HealthBells - Simple Golang Healthcheck

* Async stuff based on [Asynchronous programming with Go](https://medium.com/@gauravsingharoy/asynchronous-programming-with-go-546b96cd50c1)
* Uses hightowers [envconfig](https://github.com/kelseyhightower/envconfig) for config
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
