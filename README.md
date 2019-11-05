# p2b springboot backend

## Background
Based on [Creating a RESTful Web Service with Spring Boot](https://kotlinlang.org/docs/tutorials/spring-boot-restful.html) Source [Github](https://github.com/Kotlin/kotlin-examples/tree/master/tutorials/spring-boot-restful)

## Build locally
```shell script
$ gradle bootRun
```

## Build with docker

```shell script
$ docker build  -t p2b-server  .
$ docker run -p 8080:8080 -t p2b-server
```

## Run 

```shell script
$ curl  http:/localhost:8080/greeting?name=horst
   {"id":2,"content":"Hello, horst"}
```

## Todo

* [Let's encrypt](https://dzone.com/articles/spring-boot-secured-by-lets-encrypt)
* [DynamoDB with Kotlin and Spring Boot (Part 1)](https://tuhrig.de/dynamodb-with-kotlin-and-spring-boot/)
