FROM nginx:1-alpine
LABEL maintainer="angkor project team"
ENV API_HOST ""
ENV API_PORT "8080"
ENV DOMAIN_NAME "localhost"

# only needed if we want basic auth
#RUN apk add --no-cache --update apache2-utils=~2.4 \
#  && rm -rf /var/cache/apk/*

## only copy into the image what you really need
## use .dockerignore file to keep your build context slim and improve build performance
COPY nginx/nginx.conf /etc/nginx/nginx.conf.tmpl
COPY dist/webapp/ /www
COPY nginx/options-ssl-nginx.conf /etc/nginx/options-ssl-nginx.conf
#COPY monitoring.js.tpl /www

## before firing up the actual application process you can perform dynamic substitions
## for example replace values in static configuration files by environment variable entries
#envsubst '${monitoring_API_KEY}' < /www/monitoring.js.tpl > /www/monitoring.js && \
#htpasswd -bBc /etc/nginx/.htpasswd $BASIC_AUTH_USERNAME $BASIC_AUTH_PASSWORD && \
CMD ["sh","-c","envsubst '$DOMAIN_NAME $API_HOST $API_PORT' </etc/nginx/nginx.conf.tmpl >/etc/nginx/nginx.conf  && \
                nginx -c /etc/nginx/nginx.conf -t || cat /etc/nginx/nginx.conf  && \
                exec nginx -g 'daemon off;' " ]
