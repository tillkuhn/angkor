FROM alpine:3.12
LABEL maintainer="till"
ARG BUILD_DATE
LABEL build-date=$BUILD_DATE

#ENV BUILD_DATE=$BUILD_DATE

COPY dist/linux/imagine /bin/app

RUN apk update && apk add --no-cache ca-certificates && \
    addgroup -g 1001 appgroup && \
    adduser -H -D -s /bin/false -G appgroup -u 1001 appuser && \
    chown 1001:1001 /bin/app && \
    chmod 755 /bin/app

USER 1001:1001
WORKDIR /bin
CMD ["/bin/app"]
