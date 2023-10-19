#FROM ghcr.io/graalvm/graalvm-ce:latest as graalvm
FROM gradle AS Build

WORKDIR /app

COPY . /app

RUN gradle build -x test

FROM openjdk:17

COPY --from=Build /app/build/libs/cryptoReplenishmentProcess-0.0.1-SNAPSHOT.jar /app/cryptoReplenishmentProcess.jar

EXPOSE 8085

CMD ["java", "-jar", "/app/cryptoReplenishmentProcess.jar", "--spring.profiles.active=prod"]
