
# docker build -t drimbox-front:latest .
# docker run -p 4200:4200 drimbox-front
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

FROM nginx:1.21.6-alpine

RUN set -ex \
 && rm -r /usr/share/nginx/html

COPY --from=0 /usr/local/src/dist/SampleWebLPS /usr/share/nginx/html
COPY nginx/default.conf /etc/nginx/conf.d/
