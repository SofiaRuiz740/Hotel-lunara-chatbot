FROM node:20-alpine AS builder
WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build -- --configuration=production

FROM nginx:alpine

COPY --from=builder /app/dist/hotel-lunara-frontend/browser /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

COPY entrypoint.sh /entrypoint.sh
RUN chmod +x /entrypoint.sh

EXPOSE 8080

CMD ["/entrypoint.sh"]
