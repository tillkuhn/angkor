## Troubleshooting

* Change of user data requires new instance

```
cat /var/log/cloud-init-output.log
curl http://169.254.169.254/latest/user-data

sudo tar -C /etc -zcf /tmp/letsencrypt.tar.gz letsencrypt
letsencrypt/archive/
letsencrypt/archive/dev.timafe.net/

sudo aws s3 cp --sse=AES256 /tmp/letsencrypt.tar.gz s3://timafe-letsgo2-data/letsencrypt/letsencrypt.tar.gz
## stop nginx which is not started by certbot via systemctl 
sudo kill -s SIGQUIT $(cat /run/nginx.pid)
(...)
```

## certbot
```
sudo certbot --nginx -m <yourmail> --agree-tos -n -d <your.domain>

Saving debug log to /var/log/letsencrypt/letsencrypt.log
Plugins selected: Authenticator nginx, Installer nginx
Obtaining a new certificate
Performing the following challenges:
http-01 challenge for your.domain
nginx: [error] invalid PID number "" in "/run/nginx.pid"
Waiting for verification...
Cleaning up challenges
Deploying Certificate to VirtualHost /etc/nginx/nginx.conf
Future versions of Certbot will automatically configure the webserver so that all requests redirect to secure HTTPS access. You can control this behavior and disable this warning with the --redirect and --no-redirect flags.

- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
Congratulations! You have successfully enabled https://your.domain

You should test your configuration at:
https://www.ssllabs.com/ssltest/analyze.html?d=your.domain
- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -

IMPORTANT NOTES:
 - Congratulations! Your certificate and chain have been saved at:
   /etc/letsencrypt/live/your.domain/fullchain.pem
   Your key file has been saved at:
   /etc/letsencrypt/live/your.domain/privkey.pem
   Your cert will expire on 2020-02-12. To obtain a new or tweaked
   version of this certificate in the future, simply run certbot again
   with the "certonly" option. To non-interactively renew *all* of
   your certificates, run "certbot renew"
 - Your account credentials have been saved in your Certbot
   configuration directory at /etc/letsencrypt. You should make a
   secure backup of this folder now. This configuration directory will
   also contain certificates and private keys obtained by Certbot so
   making regular backups of this folder is ideal.
 - If you like Certbot, please consider supporting our work by:

   Donating to ISRG / Let's Encrypt:   https://letsencrypt.org/donate
   Donating to EFF:                    https://eff.org/donate-le

$
[ec2-user@ip-172-31-10-138 letsencrypt]$ ls -l /etc/letsencrypt/
insgesamt 8
drwx------ 3 root root  42 14. Nov 05:07 accounts
drwx------ 3 root root  28 14. Nov 05:07 archive
drwxr-xr-x 2 root root  34 14. Nov 05:07 csr
drwx------ 2 root root  34 14. Nov 05:07 keys
drwx------ 3 root root  42 14. Nov 05:07 live
-rw-r--r-- 1 root root 717 14. Nov 05:07 options-ssl-nginx.conf
drwxr-xr-x 2 root root  33 14. Nov 05:07 renewal
drwxr-xr-x 5 root root  43 14. Nov 05:07 renewal-hooks
-rw-r--r-- 1 root root 424 14. Nov 05:07 ssl-dhparams.pem

``` 

check if object exists in s3 bucket
aws s3api head-object --bucket timafe-letsgo2-data --key letsencrypt/letsencrypt.tar.gz



Renew just rerun

Saving debug log to /var/log/letsencrypt/letsencrypt.log
Plugins selected: Authenticator nginx, Installer nginx
Cert not yet due for renewal
Keeping the existing certificate
Deploying Certificate to VirtualHost /etc/nginx/nginx.conf
