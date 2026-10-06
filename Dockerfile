FROM gradle:9-jdk25-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle installDist --no-daemon 

FROM eclipse-temurin:25-jre-alpine

COPY --from=build /home/gradle/src/build/install/songbook /songbook
COPY --from=build /home/gradle/src/data/songs /songs
EXPOSE 8000
ENV HOST=0.0.0.0
ENV PORT=8000
ENV WEB_ROOT=/songbook/web
ENV DATA_ROOT=/data
ENV SONGS_ROOT=/songs
# Health contract shared by the mesnos services (ecosysteme/sante.md in
# mesnos/carnet.mesnos.ovh). APP_VERSION (e.g. the commit) is shown by
# /api/health: docker build --build-arg APP_VERSION=...
ARG APP_VERSION=dev
ENV APP_VERSION=$APP_VERSION
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 \
  CMD wget -qO- "http://127.0.0.1:${PORT}/api/health" || exit 1

ENTRYPOINT ["java", "-cp", "/songbook/lib/*", "songbook.server.Server"]
