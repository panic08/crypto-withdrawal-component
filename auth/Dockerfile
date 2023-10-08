FROM ubuntu:latest

WORKDIR /app

COPY . /app

RUN wget https://github.com/graalvm/graalvm-ce-builds/releases/download/vm-17.3.0/graalvm-ce-java17-linux-amd64-17.3.0.tar.gz -P /tmp \
    && tar -zxvf /tmp/graalvm-ce-java17-linux-amd64-17.3.0.tar.gz -C /opt \
    && rm /tmp/graalvm-ce-java17-linux-amd64-17.3.0.tar.gz

RUN wget https://services.gradle.org/distributions/gradle-7.1-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-7.1-bin.zip && \
    rm -rf /tmp/gradle-7.1-bin.zip

RUN gu install native-image

ENV PATH=$PATH:/opt/gradle/gradle-7.2/bin

RUN apt-get update && apt-get install -y libgomp1

RUN gradle build

EXPOSE 8080

CMD ["java -jar /build/libs/auth-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod"]




