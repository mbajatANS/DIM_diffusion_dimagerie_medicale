
# docker build -t drimbox-front-ris:latest .
# docker run -p 443:443 drimbox-front-ris
# Build
FROM node:16-buster-slim
WORKDIR /usr/local/src

COPY package.json yarn.lock ./

RUN set -ex \
 && yarn install --frozen-lockfile

COPY src src
COPY .browserslistrc angular.json tsconfig.app.json tsconfig.json proxy.conf.js ./

RUN set -ex \
 && yarn run build

FROM debian:bullseye-slim

# Generate SSL keys (self signed)
# /!\ : This is not secured and is given for sample purpose only.
# You need to put your own SSL certificates in here.
WORKDIR /root/ssl

RUN apt-get update && apt-get install -y \
  openssl \
  && rm -rf /var/lib/apt/lists/*

RUN openssl req -x509 -newkey rsa:4096 -keyout key.pem -out cert.pem -sha256 -days 365 -nodes -subj '/CN=localhost'

FROM nginx:1.21.6-alpine

RUN set -ex \
 && rm -r /usr/share/nginx/html

COPY --from=0 /usr/local/src/dist/RIS /usr/share/nginx/html
COPY --from=1 /root/ssl /etc/nginx/ssl
COPY nginx/default.conf /etc/nginx/conf.d/
