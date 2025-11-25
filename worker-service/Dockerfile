# Stage 1: Build the project
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app

# Copy only pom first for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Package the application
RUN mvn clean package -DskipTests

# Stage 2: Run the jar
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install LibreOffice (headless) and FFmpeg
RUN apk update && \
    apk add --no-cache ffmpeg libreoffice && \
    rm -rf /var/cache/apk/*

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar /app.jar

EXPOSE 8083
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:+UseG1GC", "-jar", "/app.jar"]
