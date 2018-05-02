# HMDA Platform Dockerfile
# Version: 1.0.0

FROM hseeberger/scala-sbt:scala-2.12.2-sbt-0.13.15 as build-env

ADD . /src
WORKDIR /src
RUN sbt clean assembly

# Image builds from the official Docker Java Image

FROM java:8

MAINTAINER Juan Marin Otero <juan.marin.otero@gmail.com>

WORKDIR /opt

RUN mkdir -p target && chmod -R a+w target

USER daemon

ENTRYPOINT ["java", "-jar", "hmda.jar"]

EXPOSE 8080 8081 8082

COPY --from=build-env src/target/scala-2.12/hmda.jar . 
