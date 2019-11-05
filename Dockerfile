# https://blog.codecentric.de/en/2019/08/spring-boot-heroku-docker-jdk11/

## Build
FROM gradle:jdk11 as builder
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle -Dorg.gradle.daemon=false -Dorg.gradle.caching=true assemble

## Run
FROM openjdk:11-jre-slim
VOLUME /tmp
EXPOSE 8080
COPY --from=builder /home/gradle/src/build/libs/p2b-server.jar app.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
