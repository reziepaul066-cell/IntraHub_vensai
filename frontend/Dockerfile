# Lightweight Nginx Dockerfile for Static Web Frontend

FROM nginx:alpine-slim

# Copy static frontend files to Nginx default html directory
COPY . /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
