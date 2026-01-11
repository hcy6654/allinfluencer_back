FROM gradle:8.10.2-jdk21 AS builder
WORKDIR /app

COPY settings.gradle.kts build.gradle.kts ./
COPY src ./src

RUN gradle --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV SERVER_PORT=8080

COPY --from=builder /app/build/libs/app.jar ./app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]

