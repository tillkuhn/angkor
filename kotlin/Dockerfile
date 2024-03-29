# Use Official Images for OpenJDK binaries built by Eclipse Temurin.
# https://hub.docker.com/_/eclipse-temurin/tags?page=1&name=17
#
# Default ARG values can be overwritten by passing --build-arg
# - https://docs.docker.com/engine/reference/builder/#understand-how-arg-and-from-interact
# New Java 21 LTS has arrived in September 2023 (new default unless a different version is provide as build-arg)
ARG FROM_TAG=21-jre
FROM eclipse-temurin:$FROM_TAG

# dynamic value be passed by evaluating git describe --abbrev=0
ARG LATEST_REPO_TAG=latest

# https://vsupalov.com/docker-build-time-env-values/ so we can set/overwrite at runtime
ENV APP_VERSION=$LATEST_REPO_TAG
# Possibility to set JVM options https://dzone.com/articles/spring-boot-run-and-build-in-docker
ENV JAVA_OPTS ""

# Fix CVE-2023-0286 by upgrading openssl
RUN apt-get -q update && apt-get -q upgrade -y openssl && rm -rf /var/lib/apt/lists/*

VOLUME /tmp
EXPOSE 8080

# set archiveFileName.set("app.jar") and archiveVersion.set("") in build.gradle.kts
# so the name to COPY is predicable 
COPY ./build/libs/app.jar /app.jar

# What about tweaking the heap? 
# * https://medium.com/faun/docker-sizing-java-application-326d39992592
# * https://stackoverflow.com/questions/44491257/how-to-reduce-spring-boot-memory-usage
# use shell form so we can pass JAVA_OPTS
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar" ]
