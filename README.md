# Let's go to ...

RESTful Web Service with Spring Boot, Kotlin and Gradle provisioned to AWS with terraform

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

## stop instances

 aws ec2 stop-instances --instance-ids xyz


## user
You can see all log's of your user data script and it will also create /etc/cloud folder.

/var/log/cloud-init.log and
/var/log/cloud-init-output.log



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
* [Using the Let’s Encrypt Certbot to get HTTPS on your Amazon EC2 NGINX box](https://www.freecodecamp.org/news/going-https-on-amazon-ec2-ubuntu-14-04-with-lets-encrypt-certbot-on-nginx-696770649e76/)
* [fix cerbot install issue on amazon linux 2](https://medium.com/@andrenakkurt/great-guide-thanks-for-putting-this-together-gifford-nowland-c3ce0ea2455)
* [Official aws ertificate Automation: Let's Encrypt with Certbot on Amazon Linux 2](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/SSL-on-amazon-linux-2.html#letsencrypt)
* Terraform via makefile https://github.com/pgporada/terraform-makefile
* [nginx auto reload config script](https://github.com/kubernetes/examples/blob/master/staging/https-nginx/auto-reload-nginx.sh)
* [Uploading a file to a bucket with terraform or delegate to aws cli or user archive_file](https://stackoverflow.com/questions/57456167/uploading-multiple-files-in-aws-s3-from-terraform) an d [use an AWS Lambda function to retrieve an object from S3, unzip it, then upload content back up again](https://stackoverflow.com/questions/51276201/how-to-extract-files-in-s3-on-the-fly-with-boto3)
* [Angular 8 Tutorial: Learn to Build Angular 8 CRUD Web App products](https://www.djamware.com/post/5d0eda6f80aca754f7a9d1f5/angular-8-tutorial-learn-to-build-angular-8-crud-web-app)
