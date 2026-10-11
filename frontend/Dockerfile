# STAGE 1: install dependencies and build the application
FROM node:24 AS build
WORKDIR /app
# Copies all matching files (app dependencies versions)
COPY package*.json ./
# Installs all dependencies
RUN npm ci
COPY . .
# Compiles app
RUN npm run build

# STAGE 2: generate a self-signed certificate, copy nginx configuration file and files from build stage
FROM nginx:stable-alpine3.24-perl
RUN apk add --no-cache openssl && \
    mkdir -p /etc/nginx/certs && \
    openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
      -keyout /etc/nginx/certs/selfsigned.key \
      -out /etc/nginx/certs/selfsigned.crt \
      -subj "/CN=localhost" \
      -addext "subjectAltName=DNS:localhost,IP:127.0.0.1"
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
