# Используйте базовый образ GraalVM
FROM ghcr.io/graalvm/graalvm-ce:latest as graalvm

# Установите необходимые инструменты в контейнере GraalVM
RUN gu install native-image

# Укажите рабочую директорию
WORKDIR /app

# Копируйте исходный код проекта
COPY . /app

# Установите пакеты wget и unzip с помощью apk
RUN apk update && apk add wget unzip

# Загрузите и установите Gradle
RUN wget https://services.gradle.org/distributions/gradle-7.1-bin.zip -P /tmp && \
    unzip -d /opt/gradle /tmp/gradle-7.1-bin.zip && \
    rm -rf /tmp/gradle-7.1-bin.zip

# Добавьте Gradle в переменную PATH
ENV PATH=$PATH:/opt/gradle/gradle-7.1/bin

# Установите libgomp1 с использованием apk
RUN apk update && apk add libgomp1

# Соберите проект с помощью Gradle
RUN /opt/gradle/gradle-7.1/bin/gradle build

# Выставите порт, который вы хотите экспонировать
EXPOSE 8080

# Запустите приложение
CMD ["java", "-jar", "/app/build/libs/auth-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]
