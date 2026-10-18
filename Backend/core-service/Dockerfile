# =============================================================================
# Stage 1: Maven build with dependency caching
# =============================================================================
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Layer 1: Download dependencies only (cached unless pom.xml changes)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Layer 2: Copy source and build (fast on code changes)
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true

# =============================================================================
# Stage 2: Runtime image with Java 21
# =============================================================================
FROM eclipse-temurin:21-jre
WORKDIR /app

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0"

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:InitialRAMPercentage=50.0", "-jar", "app.jar"]
