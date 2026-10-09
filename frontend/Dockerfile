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

# STAGE 2: copy nginx configuration file and files from build stage
FROM nginx:stable-alpine3.24-perl
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
