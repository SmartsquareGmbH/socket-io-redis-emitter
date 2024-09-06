FROM gradle:8-jdk17-alpine AS build

COPY . /app
WORKDIR /app

RUN --mount=type=cache,id=gradle-cache,target=/root/.gradle gradle build

FROM eclipse-temurin:17-alpine

WORKDIR /app

COPY --from=build /app/build/libs/ /app/

ENTRYPOINT ["java", "-jar", "example-backend-1.0.0-SNAPSHOT.jar"]
