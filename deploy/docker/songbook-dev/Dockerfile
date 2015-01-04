#
# SongBook Dev Dockerfile
#
# The docker image describes by this Dockerfile runs songbook from an host 
# directory for debug purposes.
# 
# A volume '/songbook-dev' must be given when running command.

# Pull base image.
FROM java:8

# Define working directory.
WORKDIR /web

# Download Vert.x
RUN \
  wget http://dl.bintray.com/vertx/downloads/vert.x-2.1.1.tar.gz && \
  tar xfz vert.x-2.1.1.tar.gz && \
  rm -f vert.x-2.1.1.tar.gz

# TODO Compute vert.x checksum
# sha1: b11d13e641e8115a8ffeff758626e950382d1a2d

#ENV HOST 0.0.0.0
ENV PORT 80
ENV WEB_ROOT /songbook-dev/web
ENV DATA_ROOT /songbook-dev/data
ENV CLASSPATH :/songbook-dev/jar/songbook.jar

EXPOSE 80

# Define default command.
CMD ["/web/vert.x-2.1.1/bin/vertx", "run", "songbook.server.Server"]
