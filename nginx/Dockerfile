FROM nginx:stable-alpine3.21-perl

# Install gettext (provides envsubst in Alpine)
RUN apk add --no-cache gettext

# Copy the template and includes into the container
COPY nginx.conf.template /etc/nginx/templates/nginx.conf.template
COPY cors.conf /etc/nginx/cors.conf
COPY static /usr/share/nginx/html

# Replace template at container start
CMD envsubst '$FRONTEND_ORIGIN' < /etc/nginx/templates/nginx.conf.template > /etc/nginx/nginx.conf && nginx -g 'daemon off;'
