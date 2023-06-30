FROM --platform=$BUILDPLATFORM maven:3.8.3-openjdk-8-slim as builder

WORKDIR build
COPY src/ ./
COPY pom.xml ./

RUN mvn -B -f ./ clean package

RUN rm -rf data

FROM alpine:latest

ENV SPI_USER="spi-provider" \
    SPI_USER_ID="1000"

RUN apk update --no-cache

RUN addgroup -g ${SPI_USER_ID} -S ${SPI_USER} && \
    adduser -u ${SPI_USER_ID} -S ${SPI_USER} -G ${SPI_USER}

WORKDIR /

USER ${SPI_USER}

COPY --from=builder /build/target/keycloak-authenticator-hmda-4.2.1.Final.jar .

