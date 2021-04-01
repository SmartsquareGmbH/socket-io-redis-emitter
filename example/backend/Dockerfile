FROM gradle:6.8.2-jdk11 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle build --no-daemon

FROM openjdk:11-jre-slim

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/ /app/

ENTRYPOINT ["java","-jar","/app/socket-io-redis-emitter-example-backend-1.0.jar"]
