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

# Copy the built jar from build stage
COPY --from=build /app/target/*.jar app.jar

ENV JAVA_OPTS="\
  -Xms64m \
  -Xmx200m \
  -XX:MaxMetaspaceSize=128m \
  -XX:CompressedClassSpaceSize=32m \
  -XX:+UseSerialGC \
  -Dserver.tomcat.max-threads=50 \
  -Dspring.main.lazy-initialization=true \
  -Dspring.datasource.hikari.maximumPoolSize=5 \
"

EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]