ARG BASE_IMAGE=openjdk
ARG BASE_TAG=16-slim

FROM jboss/keycloak:11.0.2 AS builder

COPY --chown=jboss:root scripts/ /opt/jboss/tools

RUN ${JBOSS_HOME}/bin/jboss-cli.sh --file=/opt/jboss/tools/cli/remove-datasources.cli && \
    rm -rf /opt/jboss/keycloak/standalone/configuration/standalone_xml_history && \
    rm -rf /opt/jboss/keycloak/modules/system/layers/base/com/mysql/jdbc && \
    rm -rf /opt/jboss/keycloak/modules/system/layers/base/com/h2database

# This is the base image
FROM ${BASE_IMAGE}:${BASE_TAG}

LABEL name="Keycloak" \
      description="Keycloak is an open source Identity and Access Management solution aimed at modern applications and services. It makes it easy to secure applications and services with little to no code." \
      vendor="Opensource" \
      summary="Keycloak Container" \
      maintainer="melissari_sean@bah.com" \
      version="11.0.2"

ENV KEYCLOAK_VERSION="11.0.2" \
    JDBC_POSTGRES_VERSION="42.2.5" \
    JDBC_MARIADB_VERSION="2.5.4" \
    JDBC_MSSQL_VERSION="7.4.1.jre11" \
    LAUNCH_JBOSS_IN_BACKGROUND="1" \
    PROXY_ADDRESS_FORWARDING="false" \
    JBOSS_HOME="/opt/jboss/keycloak" \
    LANG="en_US.UTF-8" \
    KEYCLOAK_USER_NAME="keycloak" \
    KEYCLOAK_USER_ID="1000" \
    KEYCLOAK_HOME="/opt/jboss"

USER root
RUN apt update -y && \
    apt full-upgrade -y && \
    apt install -y hostname openssl && \
    apt clean all && \
    rm -rf /var/cache/apt

# Copy binaries from builder image
COPY --from=builder /opt/jboss /opt/jboss

# Create the user, fix file system ownership
RUN useradd -u $KEYCLOAK_USER_ID -g 0 -M -d /opt/jboss $KEYCLOAK_USER_NAME && \
    chown -R ${KEYCLOAK_USER_NAME}:0 ${KEYCLOAK_HOME} && \
    chmod -R o-w /opt/jboss/keycloak

USER ${KEYCLOAK_USER_ID}

EXPOSE 8080 8443

HEALTHCHECK --timeout=5m --start-period=2m --retries=3 CMD curl http://localhost/auth || exit 1

ENTRYPOINT [ "/opt/jboss/tools/docker-entrypoint.sh" ]
