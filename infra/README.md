# create key pair

[Option 2: Import your own public key to Amazon EC2](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/ec2-key-pairs.html)
```
ssh-keygen -t rsa -b 4096 -C "angkor"
```
```
$ terraform validate
Success! The configuration is valid.
$ export AWS_PROFILE=<yourprofile>
$ terraform plan
```

# to be installed on instance
[Docker on Amazon Linux](https://docs.aws.amazon.com/AmazonECS/latest/developerguide/docker-basics.html)
[Working with instance user data](https://docs.aws.amazon.com/AWSEC2/latest/UserGuide/instancedata-add-user-data.html)
```
see user-data.sh
```

## letsencrypt
[](https://docs.aws.amazon.com/de_de/AWSEC2/latest/UserGuide/SSL-on-amazon-linux-2.html)

## manage IP Access from ISP
```
## Register current public IP from ISP for ssh security group ingress, inspired by:
## https://docs.aws.amazon.com/cli/latest/userguide/cli-services-ec2-sg.html
## https://stackoverflow.com/questions/46763287/i-want-to-identify-the-public-ip-of-the-terraform-execution-environment-and-add
## https://stackoverflow.com/questions/30371487/revoke-all-aws-security-group-ingress-rules
$ cat ~/bin/checkip
```
