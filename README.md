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

## Pricing

* EC2 t3a.nano	2 vcpu,	0.5 GiB $0.0054 / hour  (for 1GB / t3a.micro $0.0108)  720h (1month) x 0,0054 = $3,888 = €3,50
* [Fargate](https://aws.amazon.com/de/fargate/pricing/) pro vCPU pro Stunde	0,04656 USD   76$ pro GB pro Stunde	0,00511 USD = 1,80

## Todo

* [Let's encrypt](https://dzone.com/articles/spring-boot-secured-by-lets-encrypt)
* [DynamoDB with Kotlin and Spring Boot (Part 1)](https://tuhrig.de/dynamodb-with-kotlin-and-spring-boot/)
* [EC2](https://calculator.s3.amazonaws.com/index.html) vs [Fargate](https://aws.amazon.com/de/fargate/pricing/) Pricing
* DATA_DIR for localstack persistences
* https://github.com/salizzar/terraform-aws-docker/blob/master/main.tf
* [Authenticating with Amazon Cognito Using Spring Security](https://www.baeldung.com/spring-security-oauth-cognito)
* [Spring Boot bootstrapping class and populate the database with a few User entities](https://www.baeldung.com/spring-boot-angular-web)
* https://blog.gruntwork.io/terraform-tips-tricks-loops-if-statements-and-gotchas-f739bbae55f9
* https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html
* https://blog.codecentric.de/en/2019/05/aws-cloud-hosted-application-part-1/
* https://www.hiveit.co.uk/labs/terraform-aws-vpc-example/terraform-aws-vpc-tutorial-5-prepare-a-web-application-for-ec2
* https://github.com/benoutram/terraform-aws-vpc-example/tree/Lab-5-Prepare-a-web-application-for-ec2
