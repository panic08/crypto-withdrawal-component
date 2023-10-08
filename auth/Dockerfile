
FROM gradle AS build

WORKDIR /app

COPY . /app

RUN gradle --version

FROM ghcr.io/graalvm/graalvm-ce:latest as graalvm

RUN gradle build

RUN gu install native-image

COPY /app/build/libs/auth-0.0.1-SNAPSHOT.jar /app/auth.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/auth.jar", "--spring.profiles.active=prod"]
