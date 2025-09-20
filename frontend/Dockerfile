# -------- build stage --------
FROM node:20-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build   # -> /app/dist

# -------- runtime --------
FROM nginx:1.27-alpine

# Needed for envsubst
RUN apk add --no-cache gettext

# Your built SPA
COPY --from=build /app/dist/ /usr/share/nginx/html/

# Template and entrypoint
COPY default.tmp /etc/nginx/default.tmp
COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

EXPOSE 80
ENTRYPOINT ["/docker-entrypoint.sh"]
