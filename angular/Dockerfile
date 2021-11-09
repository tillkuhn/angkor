# tag should support linux/arm64, for updates check
# https://hub.docker.com/_/nginx?tab=tags&page=1&ordering=last_updated&name=alpine
ARG FROM_TAG=1-alpine
FROM nginx:$FROM_TAG
LABEL maintainer="Angkor Project Team"

# overwrite if docker build is not run from Dockerfile directory (e.g. from parent project root)
ARG BASEDIR=.
ARG LATEST_REPO_TAG=latest
ARG RELEASE_NAME=latest

# Avoid layers, use single ENV instruction
ENV API_HOST="" \
    API_PORT="8080" \
    DOMAIN_NAME="localhost" \
    APP_VERSION=$LATEST_REPO_TAG \
    RELEASE_NAME=$RELEASE_NAME

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
      UI_STARTED=$(date +'%Y-%m-%dT%TZ') envsubst '$APP_VERSION $RELEASE_NAME $MAPBOX_ACCESS_TOKEN $IMPRINT_URL $UI_STARTED' </www/assets/window-env.tmpl.js >/www/assets/window-env.js  && \
      DYNAMIC_ENV_CHECKSUM=$(md5sum /www/assets/window-env.js|cut -f1 -d' ') envsubst '$DYNAMIC_ENV_CHECKSUM' </www/index.tmpl.html >/www/index.html  && \
      envsubst '$SERVER_NAME_PATTERN $SERVER_NAMES $API_HOST $API_PORT $HEALTHBELLS_HOST $HEALTHBELLS_PORT $IMAGINE_HOST $IMAGINE_PORT' </etc/nginx/nginx.tmpl.conf >/etc/nginx/nginx.conf  && \
      nginx -c /etc/nginx/nginx.conf -t || cat /etc/nginx/nginx.conf  && \
      exec nginx -g 'daemon off;' "]
