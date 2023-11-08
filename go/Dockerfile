# External images to COPY selected binaries
# hadlint does not like 'latest' but as long as rubin is still under dev, we keep it
# hadolint ignore=DL3007
FROM ghcr.io/tillkuhn/rubin:latest AS rubin

# CAUTION: FROM image MUST also support linux/arm64
FROM alpine:3.18
LABEL maintainer="tillkuhn"
ARG BUILD_DATE
LABEL build-date=$BUILD_DATE
ARG RELEASE_VERSION=latest
ARG RELEASE_NAME=latest

# keep APP_VERSION for backward compatible
ENV APP_VERSION=$RELEASE_VERSION
ENV RELEASE_VERSION=$RELEASE_VERSION
ENV RELEASE_NAME=$RELEASE_NAME
#ENV BUILD_DATE=$BUILD_DATE

COPY dist/* /tools/
COPY --from=rubin /rubin /tools/rubin

# mailcap: Helper application and MIME type associations for file types
# ca-certificates: Common CA certificates PEM files from Mozilla
#
# Avoid Hadolint DL3018: Pin versions in apk add. Instead of `apk add <package>` use `apk add <package>=<version>`
# We consider this dangerous if version disappears, plus we usually always want the latest version
#
# hadolint ignore=DL3018
RUN apk update && apk add --no-cache ca-certificates mailcap && \
    addgroup -g 1001 appgroup && \
    adduser -H -D -s /bin/false -G appgroup -u 1001 appuser && \
    chown 1001:1001 /tools && \
    chmod 755 /tools/*

USER 1001:1001
ENV PATH="/tools:${PATH}"
WORKDIR /tools
