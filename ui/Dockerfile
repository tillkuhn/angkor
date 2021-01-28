# tag should support linux/arm64
ARG FROM_TAG=1-alpine
FROM nginx:$FROM_TAG
LABEL maintainer="Angkor Project Team"
ENV API_HOST ""
ENV API_PORT "8080"
ENV DOMAIN_NAME "localhost"

# overwrite if not called from Dockerfile diretory (e.g. from project root)
ARG BASEDIR=.
ARG LATEST_REPO_TAG=latest
ENV APP_VERSION=$LATEST_REPO_TAG

# only needed if we want basic auth
#RUN apk add --no-cache --update apache2-utils=~2.4 && rm -rf /var/cache/apk/*

## only copy into the image what you really need
## use .dockerignore file to keep your build context slim and improve build performance
COPY ${BASEDIR}/nginx/nginx.conf /etc/nginx/nginx.tmpl.conf
COPY ${BASEDIR}/dist/webapp/ /www
COPY ${BASEDIR}/nginx/options-ssl-nginx.conf /etc/nginx/options-ssl-nginx.conf
COPY ${BASEDIR}/nginx/options-proxy-nginx.conf /etc/nginx/options-proxy-nginx.conf

## before firing up the actual application process you can perform dynamic substitions
## for example replace values in static configuration files by environment variable entries
# DL3025Use arguments JSON notation for CMD and ENTRYPOINT arguments, false positive since this *is* json
# hadolint ignore=DL3025
CMD ["sh","-c", "cp /www/index.html /www/index.tmpl.html && \
      envsubst '$APP_VERSION $MAPBOX_ACCESS_TOKEN $IMPRINT_URL' </www/assets/window-env.tmpl.js >/www/assets/window-env.js  && \
      DYNAMIC_ENV_CHECKSUM=$(md5sum /www/assets/window-env.js|cut -f1 -d' ') envsubst '$DYNAMIC_ENV_CHECKSUM' </www/index.tmpl.html >/www/index.html  && \
      envsubst '$SERVER_NAME_PATTERN $SERVER_NAMES $API_HOST $API_PORT $HEALTHBELLS_HOST $HEALTHBELLS_PORT $IMAGINE_HOST $IMAGINE_PORT' </etc/nginx/nginx.tmpl.conf >/etc/nginx/nginx.conf  && \
      nginx -c /etc/nginx/nginx.conf -t || cat /etc/nginx/nginx.conf  && \
      exec nginx -g 'daemon off;' "]
