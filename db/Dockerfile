FROM postgres:16-alpine
LABEL org.opencontainers.image.source=https://github.com/Turtleshell2077/factory-control
COPY init.sql /docker-entrypoint-initdb.d/init.sql
EXPOSE 5432
