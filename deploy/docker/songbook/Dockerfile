#
# SongBook Dockerfile
#
# 

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

# Download Songbook
RUN \
  wget https://github.com/kawane/songbook/releases/download/0.3.1/songbook.zip && \
  unzip songbook.zip && \
  rm -f songbook.zip

ENV HOST 0.0.0.0
ENV PORT 80
ENV WEB_ROOT /web/songbook/web
ENV DATA_ROOT /web/songbook/data
ENV CLASSPATH :/web/songbook/jar/songbook.jar

EXPOSE  80

# Define default command.
CMD ["/web/vert.x-2.1.1/bin/vertx", "run", "songbook.server.Server"]
