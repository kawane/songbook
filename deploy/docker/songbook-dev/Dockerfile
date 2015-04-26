#
# SongBook Dev Dockerfile
#
# The docker image describes by this Dockerfile runs songbook from an host 
# directory for debug purposes.
# 
# A volume '/songbook-dev' must be given when running command.

# Pull base image.
FROM java:8

# Defines data volume
VOLUME /data

# Define working directory.
WORKDIR /web

ENV HOST 0.0.0.0
ENV PORT 80
ENV WEB_ROOT /songbook-dev/web
ENV DATA_ROOT /songbook-dev/data

EXPOSE 80

WORKDIR /songbook-0.4

# Define default command.
CMD ["./bin/songbook"]
