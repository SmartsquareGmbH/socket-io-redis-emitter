FROM gradle:jdk25-alpine AS build

WORKDIR /app
COPY --chown=gradle:gradle . .
RUN ./gradlew installDist --no-daemon

FROM eclipse-temurin:25-alpine

WORKDIR /app
COPY --from=build /app/build/install/example-backend/ /app/

ENTRYPOINT ["/app/bin/example-backend"]
