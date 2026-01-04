FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

COPY pom.xml ./
COPY src ./src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

ENV SERVER_PORT=3001

COPY --from=builder /app/target/*.jar ./app.jar

EXPOSE 3001
CMD ["java", "-jar", "app.jar"]

