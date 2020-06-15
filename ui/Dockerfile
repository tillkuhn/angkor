FROM nginx:1-alpine
LABEL maintainer="angkor project team"
ENV BASIC_AUTH_USERNAME none
ENV BASIC_AUTH_PASSWORD none

## that would be the place to install additional tools if needed
## remember to pin at least major versions b/c of https://github.com/hadolint/hadolint/wiki/DL3018
RUN apk add --no-cache --update apache2-utils=~2.4 \
  && rm -rf /var/cache/apk/*

## only copy into the image what you really need
## use .dockerignore file to keep your build context slim and improve build performance
COPY nginx.conf /etc/nginx/nginx.conf.tmpl
#COPY default.conf /etc/nginx/conf.d/default.conf.tmpl
#COPY src /www
COPY dist/webapp/ /www
#COPY monitoring.js.tpl /www

## before firing up the actual application process you can perform dynamic substitions
## for example replace values in static configuration files by environment variable entries
#envsubst '${monitoring_API_KEY}' < /www/monitoring.js.tpl > /www/monitoring.js && \
CMD ["sh","-c","htpasswd -bBc /etc/nginx/.htpasswd $BASIC_AUTH_USERNAME $BASIC_AUTH_PASSWORD && \
                 envsubst '$VAR1 $VAR1' </etc/nginx/nginx.conf.tmpl >/etc/nginx/nginx.conf  && \
                 nginx -c /etc/nginx/nginx.conf -t || cat /etc/nginx/nginx.conf  && \
                 exec nginx -g 'daemon off;' " ]
