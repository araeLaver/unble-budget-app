# Use OpenJDK 17 and install Maven
FROM openjdk:17-jdk-slim

# Install Maven
RUN apt-get update && \
    apt-get install -y maven && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

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

# Expose port 8080
EXPOSE 8080

# Run the application with optimized JVM settings
CMD ["java", "-Xms256m", "-Xmx512m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=200", "-jar", "app.jar", "--spring.profiles.active=prod"]