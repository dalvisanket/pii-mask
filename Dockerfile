FROM maven:3.8.4-openjdk-17-slim AS build
WORKDIR /app

COPY pom.xml /app

RUN mvn dependency:go-offline -B

COPY src /app/src

RUN mvn clean package -Dmaven.test.skip=true

FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /app/target/*.jar pii.jar

ENTRYPOINT ["java", "-jar", "pii.jar"]
