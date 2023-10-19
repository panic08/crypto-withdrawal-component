#FROM ghcr.io/graalvm/graalvm-ce:latest as graalvm
FROM gradle AS Build

WORKDIR /app

COPY . /app

RUN gradle build -x test

FROM openjdk:17

COPY --from=Build /app/build/libs/userApi-0.0.1-SNAPSHOT.jar /app/userApi.jar

EXPOSE 8081

CMD ["java", "-jar", "/app/userApi.jar", "--spring.profiles.active=prod"]
