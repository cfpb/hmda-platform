# HMDA Platform Dockerfile
# Version: 1.0.0

# Image builds from the official Docker Java Image

FROM java:8

MAINTAINER Juan Marin Otero <juan.marin.otero@gmail.com>

WORKDIR /opt

RUN mkdir -p target && chmod -R a+w target

USER daemon

ENTRYPOINT ["java", "-jar", "hmda.jar"]

EXPOSE 10003 

COPY target/scala-2.11/hmda.jar . 
