
FROM ubuntu:latest as installer

RUN apt-get update && apt-get install -y unzip

WORKDIR /app

COPY . /app

RUN curl -L https://services.gradle.org/distributions/gradle-6.5.1-bin.zip -o gradle-6.5.1-bin.zip
RUN unzip gradle-6.5.1-bin.zip
ENV GRADLE_HOME=/gradle/gradle-6.5.1
ENV PATH=$PATH:$GRADLE_HOME/bin
RUN gradle --version

FROM ghcr.io/graalvm/graalvm-ce:latest as graalvm

RUN gradle build

RUN gu install native-image

WORKDIR /app

COPY /app/build/libs/auth-0.0.1-SNAPSHOT.jar /app/auth.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/auth.jar", "--spring.profiles.active=prod"]
