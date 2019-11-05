# p2b springboot backend

## Background
Based on [Creating a RESTful Web Service with Spring Boot](https://kotlinlang.org/docs/tutorials/spring-boot-restful.html) Source [Github](https://github.com/Kotlin/kotlin-examples/tree/master/tutorials/spring-boot-restful)

## Build locally
```shell script
$ gradle bootRun  --args='--spring.profiles.active=dynamo-local'
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


## DynamoDB Integration

```shell script
pip install --upgrade localstack
SERVICES=dynamodb DEFAULT_REGION=eu-central-1  localstack start --host
  Starting mock DynamoDB (http port 4569)...
awslocal dynamodb list-tables
aws dynamodb --endpoint-url http://localhost:4569 list-tables
awslocal dynamodb scan --table-name=Place
```
## Todo

* [Let's encrypt](https://dzone.com/articles/spring-boot-secured-by-lets-encrypt)
* [DynamoDB with Kotlin and Spring Boot (Part 1)](https://tuhrig.de/dynamodb-with-kotlin-and-spring-boot/)
* [EC2](https://calculator.s3.amazonaws.com/index.html) vs [Fargate](https://aws.amazon.com/de/fargate/pricing/) Pricing
