
FROM ghcr.io/graalvm/graalvm-ce:latest as graalvm

RUN gu install native-image

WORKDIR /app

COPY . /app

RUN apt-get update && apt-get install -y wget unzip

RUN wget https://services.gradle.org/distributions/gradle-7.1-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-7.1-bin.zip && \
    rm -rf /tmp/gradle-7.1-bin.zip

ENV PATH=$PATH:/opt/gradle/gradle-7.1/bin

RUN apt-get update && apt-get install -y libgomp1

RUN /opt/gradle/gradle-7.1/bin/gradle build

EXPOSE 8080

CMD ["java", "-jar", "/app/build/libs/auth-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]
