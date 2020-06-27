ARG FROM_TAG=1-alpine
FROM nginx:$FROM_TAG
LABEL maintainer="angkor project team"
ENV API_HOST ""
ENV API_PORT "8080"
ENV DOMAIN_NAME "localhost"

# should be passed in from git describe --abbrev=0
ARG LATEST_REPO_TAG=latest
# https://vsupalov.com/docker-build-time-env-values/ so we can use at runtime
ENV VERSION=$LATEST_REPO_TAG

# only needed if we want basic auth
#RUN apk add --no-cache --update apache2-utils=~2.4 && rm -rf /var/cache/apk/*

## only copy into the image what you really need
## use .dockerignore file to keep your build context slim and improve build performance
COPY nginx/nginx.conf /etc/nginx/nginx.tmpl.conf
COPY dist/webapp/ /www
COPY nginx/options-ssl-nginx.conf /etc/nginx/options-ssl-nginx.conf

## before firing up the actual application process you can perform dynamic substitions
## for example replace values in static configuration files by environment variable entries
#envsubst '${monitoring_API_KEY}' < /www/monitoring.js.tpl > /www/monitoring.js && \
#htpasswd -bBc /etc/nginx/.htpasswd $BASIC_AUTH_USERNAME $BASIC_AUTH_PASSWORD && \
CMD ["sh","-c", "cp /www/index.html /www/index.tmpl.html && \
      envsubst '$VERSION $MAPBOX_ACCESS_TOKEN' </www/assets/window-env.tmpl.js >/www/assets/window-env.js  && \
      DYNAMIC_ENV_CHECKSUM=$(md5sum /www/assets/window-env.js|cut -f1 -d' ') envsubst '$DYNAMIC_ENV_CHECKSUM' </www/index.tmpl.html >/www/index.html  && \
      envsubst '$SERVER_NAME_PATTERN $SERVER_NAMES $API_HOST $API_PORT' </etc/nginx/nginx.tmpl.conf >/etc/nginx/nginx.conf  && \
      nginx -c /etc/nginx/nginx.conf -t || cat /etc/nginx/nginx.conf  && \
      exec nginx -g 'daemon off;' " ]
