#FROM ghcr.io/graalvm/graalvm-ce:latest as graalvm
FROM gradle AS Build

WORKDIR /app

COPY . /app

RUN gradle build -x test

FROM openjdk:17

COPY --from=Build /app/build/libs/replenishmentApi-0.0.1-SNAPSHOT.jar /app/replenishmentApi.jar

EXPOSE 8083

CMD ["java", "-jar", "/app/replenishmentApi.jar", "--spring.profiles.active=prod"]
