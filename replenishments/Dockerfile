#FROM ghcr.io/graalvm/graalvm-ce:latest as graalvm
FROM gradle AS Build

WORKDIR /app

COPY . /app

RUN gradle build -x test

FROM openjdk:17

COPY --from=Build /app/build/libs/replenishments-0.0.1-SNAPSHOT.jar /app/replenishments.jar

EXPOSE 8084

CMD ["java", "-jar", "/app/replenishments.jar", "--spring.profiles.active=prod"]
