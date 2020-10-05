# SQS Poller golang

## Local test publish
```
aws sns publish --profile timafe --topic-arn arn:aws:sns:eu-central-1:<account>:angkor-events  --message 'huhu'
```

## Sample S3 Notification triggered by bucket upload
[s3 notification-content-structure](https://docs.aws.amazon.com/de_de/AmazonS3/latest/dev/notification-content-structure.html)

```
{  
   "Records":[  
      {  
         "eventVersion":"2.2",
         "eventSource":"aws:s3",
         "awsRegion":"eu-central-1",
         "eventTime":The time, in ISO-8601 format, for example, 1970-01-01T00:00:00.000Z, when Amazon S3 finished processing the request,
         "eventName":"event-type",
     }
 ]
```

## Sample SQS Message

```
{
  "event": "s3push",
  "workflow": "golang-ci"
}
```
MD5 of message body: af83e4e084ff1ae3666f6586897230d0

Attributes:
GITHUB_RUN_ID,String,281175730
GITHUB_SHA,String,dca5617d54b228075f01fd1642631d37180518b9
