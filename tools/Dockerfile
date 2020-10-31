FROM alpine:3.12
LABEL maintainer="till"
ARG BUILD_DATE
LABEL build-date=$BUILD_DATE

#ENV BUILD_DATE=$BUILD_DATE

COPY dist/* /tools/

RUN apk update && apk add --no-cache ca-certificates && \
    addgroup -g 1001 appgroup && \
    adduser -H -D -s /bin/false -G appgroup -u 1001 appuser && \
    chown 1001:1001 /tools&& \
    chmod 755 /tools/*

USER 1001:1001
ENV PATH="/tools:${PATH}"
WORKDIR /tools
#CMD ["/bin/app"]
