FROM node:15.3.0-alpine as base_build

ARG SERVER_ENDPOINT 
ENV SERVER_ENDPOINT ${SERVER_ENDPOINT}

RUN mkdir -p /app
COPY ./client /app

WORKDIR "/app"
RUN apk add python make g++

RUN yarn install && yarn generate


FROM nginx:1.19-alpine

COPY ./docker/client/nginx.conf /etc/nginx/nginx.conf
COPY --from=base_build /app/dist /app
