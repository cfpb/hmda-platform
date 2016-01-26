# HMDA Platform Dockerfile
# Version: 1.0.0

# Image builds from the official Docker Java Image

FROM java:8

MAINTAINER Juan Marin Otero <juan.marin.otero@gmail.com>

WORKDIR /

USER daemon

ENTRYPOINT ["java", "-jar", "/opt/hmda.jar"]

EXPOSE 8080

COPY target/scala-2.11/hmda.jar /opt/hmda.jar