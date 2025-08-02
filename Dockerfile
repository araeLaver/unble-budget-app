# Multi-stage build for smaller image
FROM maven:3.9-eclipse-temurin-17-alpine AS builder

# Set working directory
WORKDIR /app

# Copy pom.xml first (for better Docker layer caching)
COPY pom.xml .

# Download dependencies (cached if pom.xml hasn't changed)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ src/

# Build the application
RUN mvn clean package -DskipTests

# List files to debug
RUN ls -la target/

# Find and copy the JAR file (handle both possible names)
RUN if [ -f target/unble-budget-app-1.0.0.jar ]; then \
        cp target/unble-budget-app-1.0.0.jar app.jar; \
    elif [ -f target/unble-budget-0.0.1-SNAPSHOT.jar ]; then \
        cp target/unble-budget-0.0.1-SNAPSHOT.jar app.jar; \
    else \
        cp target/*.jar app.jar; \
    fi

# Verify the JAR file exists
RUN ls -la app.jar

# Use smaller runtime image
FROM eclipse-temurin:17-jre-alpine

# Copy JAR from builder stage
COPY --from=builder /app/app.jar /app.jar

# Expose port 8080
EXPOSE 8080

# Run the application optimized for larger instance  
CMD ["java", "-Xmx200m", "-Xms100m", "-XX:+UseSerialGC", "-XX:MaxMetaspaceSize=128m", "-XX:MetaspaceSize=64m", "-XX:+UseContainerSupport", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar", "--spring.profiles.active=prod"]