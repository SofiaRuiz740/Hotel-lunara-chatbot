#!/bin/sh
set -eu

# Runtime-config: allows overriding the API base URL without rebuilding the frontend.
# If API_BASE_URL is empty, the app uses same-origin `/api` and nginx proxies it.

API_BASE_URL="${API_BASE_URL:-}"

mkdir -p /usr/share/nginx/html/assets

# Escape backslashes and double-quotes for safe JS string injection.
API_BASE_URL_ESCAPED="$(printf '%s' "$API_BASE_URL" | sed -e 's/\\/\\\\/g' -e 's/"/\\"/g')"

cat > /usr/share/nginx/html/assets/runtime-config.js <<EOF
window.__API_URL__ = "${API_BASE_URL_ESCAPED}";
EOF

exec nginx -g "daemon off;"
