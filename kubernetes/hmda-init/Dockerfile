FROM eclipse-temurin:23.0.1_11-jdk-alpine

RUN apk upgrade --update-cache --available && apk add openssl
RUN adduser -H -S -G root -h /opt hmda
RUN mkdir /opt/docker
RUN chmod -R 777 /opt
RUN chown -R hmda /opt

USER hmda