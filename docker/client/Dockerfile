FROM node:15.3.0-alpine as base_build

# node + nginx dockerfile template

RUN mkdir -p /app
COPY ./client /app

WORKDIR "/app"

RUN apk add python make g++
RUN yarn install && yarn generate

FROM nginx:1.19-alpine

COPY ./docker/client/nginx.conf /etc/nginx/nginx.conf
COPY --from=base_build /app/dist /app
