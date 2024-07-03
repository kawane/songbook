FROM gradle:8-jdk21-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle installDist --no-daemon 

FROM eclipse-temurin:21-jre-alpine

COPY --from=build /home/gradle/src/build/install/songbook /songbook
COPY --from=build /home/gradle/src/data/songs /songs
EXPOSE 8000
ENV HOST=0.0.0.0
ENV PORT=8000
ENV WEB_ROOT=/songbook/web
ENV DATA_ROOT=/data
ENV SONGS_ROOT=/songs

ENTRYPOINT ["java", "-cp", "/songbook/lib/*", "songbook.server.Server"]
