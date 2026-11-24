# syntax=docker/dockerfile:1
# "app" es la carpeta del frontend (FRONTEND_PATH). Solo se lee: nada se escribe en ella.
FROM node:22-alpine AS build
WORKDIR /app
COPY --from=app package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --no-audit --no-fund
COPY --from=app angular.json tsconfig.json tsconfig.app.json ./
COPY --from=app src src
RUN npx ng build --configuration production --output-path /salida

FROM nginx:1.28-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /salida /usr/share/nginx/html
