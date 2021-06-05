# https://docs.docker.com/engine/reference/builder/#understand-how-arg-and-from-interact
# default can be overwritten by --build-arg
# https://hub.docker.com/r/adoptopenjdk/openjdk16
ARG FROM_TAG=aarch64-ubuntu-jre-16.0.1_9
FROM adoptopenjdk/openjdk16:$FROM_TAG
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
