
FROM alpine:latest as installer

RUN apk update && apk add wget unzip

WORKDIR /app

COPY . /app

RUN wget https://services.gradle.org/distributions/gradle-7.1-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-7.1-bin.zip && \
    rm -rf /tmp/gradle-7.1-bin.zip

ENV PATH=$PATH:/opt/gradle/gradle-7.1/bin

FROM ghcr.io/graalvm/graalvm-ce:latest as graalvm

RUN /opt/gradle/gradle-7.1/bin/gradle build

RUN gu install native-image

WORKDIR /app

COPY --from=installer /app/build/libs/auth-0.0.1-SNAPSHOT.jar /app/auth.jar

EXPOSE 8080

CMD ["java", "-jar", "/app/auth.jar", "--spring.profiles.active=prod"]
