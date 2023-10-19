#FROM ghcr.io/graalvm/graalvm-ce:latest as graalvm
FROM gradle AS Build

WORKDIR /app

COPY . /app

RUN gradle build -x test

FROM openjdk:17

COPY --from=Build /app/build/libs/auth-0.0.1-SNAPSHOT.jar /app/auth.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/auth.jar", "--spring.profiles.active=prod"]
