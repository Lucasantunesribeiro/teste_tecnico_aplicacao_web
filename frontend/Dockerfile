# syntax=docker/dockerfile:1.7

# Stage 1: Build
FROM node:22-alpine AS build
WORKDIR /app

# Cache dependencies
COPY package*.json ./
RUN --mount=type=cache,target=/root/.npm npm ci

# Build application
COPY . .
RUN npm run build

# Stage 2: Serve with nginx
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# Copy nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built assets
COPY --from=build /app/dist .

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD wget -qO- http://localhost/index.html || exit 1
