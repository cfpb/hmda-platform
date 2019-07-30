FROM node:8.16.0-jessie as build-stage

RUN mkdir -p /home/newman-trials/scripts/
RUN mkdir -p /home/newman-trials/hmda-filing/
WORKDIR /home/newman-trials

COPY dev/hmda-filing/*  hmda-filing/
COPY scripts/*  scripts/
COPY newman-pak/newman-4.5.1.tgz  .
RUN npm i  --prefix ./ newman-4.5.1.tgz
RUN chmod -R 755 /home/newman-trials
