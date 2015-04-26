#
# SongBook Dockerfile
#
# 

# Pull base image.
FROM java:8

# Defines data volume
VOLUME /data

# Get Songbook
ADD songbook-0.4.zip .

RUN \
  unzip songbook-0.4.zip && \
  rm -f songbook-0.4.zip

ENV HOST 0.0.0.0
ENV PORT 80
ENV WEB_ROOT /songbook-0.4/web
ENV DATA_ROOT /data

EXPOSE 80

WORKDIR /songbook-0.4

# Define default command.
CMD ["./bin/songbook"]
