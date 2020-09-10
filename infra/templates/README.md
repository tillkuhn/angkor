# Note 
variables in template files file are substitued by terraform templates
 so you need to use double dollar signs ($) to escape variables

```
# $$ will be treated as is by templatefile and end up as $
# note $(command ...) is ok
SCRIPT=$(basename $${BASH_SOURCE[0]})
```

```
# ${appid} will be substituted
image: ${docker_user}/${appid}-api:${api_version}
```
