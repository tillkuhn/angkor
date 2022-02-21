# default ARG values can be overwritten by passing --build-arg
# - https://docs.docker.com/engine/reference/builder/#understand-how-arg-and-from-interact
# For adoptopenjdk/openjdk16 Image Updates, check the following URL
# - https://hub.docker.com/r/adoptopenjdk/openjdk16/tags?page=1&ordering=last_updated&name=aarch64-ubuntu-jre
# FROM adoptopenjdk/openjdk16:aarch64-ubuntu-jre-16.0.1_9
# For Java17, we give https://hub.docker.com/_/eclipse-temurin?tab=tags&page=1&name=17 a try
# since AdoptOpenJDK has been deprecated in favor of live Eclipse Adoptium, and also support a JRE Variant
# as well as support for for ARM Architectures, and it's available via SDKMAN!
ARG FROM_TAG=17-jre-centos7
FROM eclipse-temurin:$FROM_TAG

# dynamic value be passed by evaluating git describe --abbrev=0
ARG LATEST_REPO_TAG=latest

# https://vsupalov.com/docker-build-time-env-values/ so we can set/overwrite at runtime
ENV APP_VERSION=$LATEST_REPO_TAG
# Possibility to set JVM options https://dzone.com/articles/spring-boot-run-and-build-in-docker
ENV JAVA_OPTS ""

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
