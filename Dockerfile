FROM gradle:8.0.2-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle installDist --no-daemon 

FROM eclipse-temurin:17-jre

COPY --from=build /home/gradle/src/build/install/songbook /songbook
COPY --from=build /home/gradle/src/data/songs /songs
EXPOSE 8000
ENV HOST=0.0.0.0
ENV PORT=8000
ENV WEB_ROOT=/songbook/web
ENV DATA_ROOT=/data
ENV SONGS_ROOT=/songs

ENTRYPOINT ["java", "-cp", "/songbook/lib/*", "songbook.server.Server"]
