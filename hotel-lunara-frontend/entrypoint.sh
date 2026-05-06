#!/bin/sh
set -eu

# Runtime-config: allows injecting the backend URL without rebuilding the frontend.
# Cloud Run can set API_BASE_URL, and this file will expose it as `window.__API_URL__`.

API_BASE_URL="${API_BASE_URL:-https://hotel-lunara-backend-xxxx.run.app}"

mkdir -p /usr/share/nginx/html/assets

# Escape backslashes and double-quotes for safe JS string injection.
API_BASE_URL_ESCAPED="$(printf '%s' "$API_BASE_URL" | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g')"

cat > /usr/share/nginx/html/assets/runtime-config.js <<EOF
window.__API_URL__ = "${API_BASE_URL_ESCAPED}";
EOF

exec nginx -g "daemon off;"
