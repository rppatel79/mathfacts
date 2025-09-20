#!/bin/sh
set -eu

if [ -z "${API_ORIGIN:-}" ]; then
  echo "ERROR: API_ORIGIN is not set (e.g. http://backend:8080)"
  exit 1
fi

# Only substitute API_ORIGIN, not every $VAR
envsubst '${API_ORIGIN}' < /etc/nginx/default.tmp > /etc/nginx/conf.d/default.conf

echo "---- RENDERED /etc/nginx/conf.d/default.conf ----"
sed -n '1,200p' /etc/nginx/conf.d/default.conf
echo "-------------------------------------------------"

exec nginx -g 'daemon off;'
