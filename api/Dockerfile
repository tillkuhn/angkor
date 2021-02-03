# https://docs.docker.com/engine/reference/builder/#understand-how-arg-and-from-interact
# default can be overwritten by --build-arg
#ARG FROM_TAG=jre-14.0.2_12-alpine
ARG FROM_TAG=aarch64-ubuntu-jre-14.0.2_12
# x86_64-ubuntu-jre-14.0.2_12
FROM adoptopenjdk/openjdk14:$FROM_TAG
# should be passed in from git describe --abbrev=0
ARG LATEST_REPO_TAG=latest
# https://vsupalov.com/docker-build-time-env-values/ so we can use at runtime
ENV APP_VERSION=$LATEST_REPO_TAG
# Possibility to set JVM options https://dzone.com/articles/spring-boot-run-and-build-in-docker
ENV JAVA_OPTS ""
VOLUME /tmp
EXPOSE 8080
COPY ./build/libs/app.jar /app.jar
# what about heap ? https://medium.com/faun/docker-sizing-java-application-326d39992592
# and https://stackoverflow.com/questions/44491257/how-to-reduce-spring-boot-memory-usage
# use shell form to support JAVA_OPTS
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar" ]
