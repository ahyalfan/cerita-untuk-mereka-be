# Stage 1: Build
FROM maven:3.9.8-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Run
FROM openjdk:17-slim-bullseye
WORKDIR /app
COPY --from=build /app/target/Cerita-Untuk-Mereka.jar /app/Cerita-Untuk-Mereka.jar
ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_USERNAME=root
ENV DB_PASSWORD=root
ENV DB_PORT=5432
ENV DB_HOST=postgres
ENTRYPOINT ["java", "-jar", "/app/Cerita-Untuk-Mereka.jar"]
EXPOSE 8081
