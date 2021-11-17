FROM alpine:latest

ENV THEME_PROVIDER_USER="themeprovider" \
    THEME_PROVIDER_USER_ID="1000"

RUN apk update --no-cache


# Create the user, fix file system ownership
RUN addgroup -g ${THEME_PROVIDER_USER_ID} -S ${THEME_PROVIDER_USER} && \
    adduser -u ${THEME_PROVIDER_USER_ID} -S ${THEME_PROVIDER_USER} -G ${THEME_PROVIDER_USER}

USER ${THEME_PROVIDER_USER}
COPY themes/hmda /themes

