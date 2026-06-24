FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /workspace
COPY . .
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /workspace/business-application/target/business-application-1.0-SNAPSHOT-exec.jar /app/fool-service.jar

EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "/app/fool-service.jar"]
