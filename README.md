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

## SSH Keys and build infra

```
ssh-keygen -f./mykey.pem
terraform init
terraform plan
terraform apply -auto-approve

scp -i mykey.pem build/libs/p2b-server.jar  ec2-user@sudo amazon-linux-extras install nginx1@10.52.187.19:/home/ec2-user
ssh -i mykey.pem ec2-user@<IP-ADDRESS-OUTPUT>

sudo yum update -y
sudo amazon-linux-extras install java-openjdk11 -y
sudo amazon-linux-extras install nginx1 -y
java -jar /home/ec2-user/p2b-server.jar
```
Install [Java 11](https://tecadmin.net/install-java-on-amazon-linux/)
[Install Certbot on Amazon Linux – A smart way to enable LetsEncrypt](https://bobcares.com/blog/install-certbot-on-amazon-linux/)

## Pricing

* [EC2](https://aws.amazon.com/ec2/instance-types/?nc1=h_ls) t3a.nano	2 vcpu,	0.5 GiB $0.0054 / hour  (for 1GB / t3a.micro $0.0108)  720h (1month) x 0,0054 = $3,888 = €3,50
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

* https://medium.com/@saurabh6790/generate-wildcard-ssl-certificate-using-lets-encrypt-certbot-273e432794d7
* https://www.freecodecamp.org/news/going-https-on-amazon-ec2-ubuntu-14-04-with-lets-encrypt-certbot-on-nginx-696770649e76/
