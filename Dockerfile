FROM openjdk:11-jre-slim
#ENV JAVA_OPTS ""
VOLUME /tmp
EXPOSE 8080
COPY ./build/libs/app.jar /app.jar
## what about heap ? https://medium.com/faun/docker-sizing-java-application-326d39992592
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar"]
