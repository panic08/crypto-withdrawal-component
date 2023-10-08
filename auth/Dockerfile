FROM ubuntu:latest
FROM ghcr.io/graalvm/graalvm-ce:latest as graalvm

WORKDIR /app
COPY . /app

RUN apt-get update && apt-get install -y wget unzip

RUN wget https://services.gradle.org/distributions/gradle-7.1-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-7.1-bin.zip && \
    rm -rf /tmp/gradle-7.1-bin.zip

RUN gu install native-image

ENV PATH=$PATH:/opt/gradle/gradle-7.2/bin

RUN apt-get update && apt-get install -y libgomp1

RUN gradle build

EXPOSE 8080

CMD ["java -jar /build/libs/auth-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod"]




