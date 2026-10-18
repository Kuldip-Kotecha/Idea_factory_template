# Multi-stage build for utility-service with 3 submodules (ai, tts, scraping)
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy parent pom.xml and submodule pom.xml files first for dependency resolution
COPY pom.xml ./
COPY parent/pom.xml ./parent/
COPY ai/pom.xml ./ai/
COPY tts/pom.xml ./tts/
COPY scraping/pom.xml ./scraping/

# Download dependencies for all modules
RUN mvn dependency:go-offline

# Copy source code for all submodules
COPY ai/src ./ai/src
COPY tts/src ./tts/src
COPY scraping/src ./scraping/src

# Build all submodules
RUN mvn clean package -DskipTests

# Use standard light image for runtime
FROM eclipse-temurin:21-jre
WORKDIR /app

# Install Playwright browser dependencies for web scraping
RUN apt-get update && apt-get install -y --no-install-recommends \
    libglib2.0-0 libnss3 libnspr4 libdbus-1-3 libatk1.0-0 \
    libatk-bridge2.0-0 libcups2 libdrm2 libxcb1 libxkbcommon0 \
    libatspi2.0-0 libx11-6 libxcomposite1 libxdamage1 libxext6 \
    libxfixes3 libxrandr2 libgbm1 libpango-1.0-0 libcairo2 libasound2t64 \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/ai/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-XX:InitialRAMPercentage=50.0", "-jar", "app.jar"]