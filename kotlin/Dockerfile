# Use Official Images for OpenJDK binaries built by Eclipse Temurin,
# but pull from public ECR registry to bypass docker rate limit issues
# Look around: https://gallery.ecr.aws/docker/library/eclipse-temurin
# CAUTION: FROM image tag must support linux/arm64
# Tags with suffix 'jre-noble' seem to offer the smallest image size (e.g. 25-jre-noble ~109MB)
FROM public.ecr.aws/docker/library/eclipse-temurin:25-jre-noble

# Default ARG values can be overwritten by passing --build-arg
# @see https://docs.docker.com/engine/reference/builder/#understand-how-arg-and-from-interact
# dynamic value can be passed by evaluating result of git describe --abbrev=0
ARG LATEST_REPO_TAG=latest

# Example OS Library Patch: Fix CVEs by upgrading openssl
# RUN apt-get -q update && apt-get -q upgrade -y openssl && rm -rf /var/lib/apt/lists/*

# Use a non-root user for better security.
RUN addgroup --system appgroup && adduser --system --ingroup appgroup appuser
WORKDIR /app

# add archiveFileName.set("app.jar") and archiveVersion.set("") to build.gradle.kts
# so the artifact name in the COPY directive is always predicable
COPY ./build/libs/app.jar /app/app.jar
RUN chown -R appuser:appgroup /app
USER appuser

# VOLUME /tmp allows the /tmp directory to persist data outside the container lifecycle, which is useful for temporary files created by the app or the JVM.
# Some Spring Boot features (like file uploads or embedded Tomcat) use /tmp for temporary storage.
VOLUME /tmp
# Describe (!) which ports your application is listening on.
EXPOSE 8080

# Default Docker Env Vars during build, can be overwritten at runtime @see https://vsupalov.com/docker-build-time-env-values/
# More on overwriting JVM options @see https://dzone.com/articles/spring-boot-run-and-build-in-docker
ENV JAVA_OPTS=""
ENV APP_VERSION=$LATEST_REPO_TAG

# What about tweaking the heap?
# @see https://medium.com/faun/docker-sizing-java-application-326d39992592
# @see https://stackoverflow.com/questions/44491257/how-to-reduce-spring-boot-memory-usage
# use shell form for ENTRYPOINT so we can pass JAVA_OPTS
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar" ]
