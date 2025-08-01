# Use OpenJDK 17 as base image
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Copy source code
COPY src/ src/

# Make mvnw executable (for Unix systems)
RUN chmod +x ./mvnw

# Build the application
RUN ./mvnw clean package -DskipTests

# Expose port 8080 (Koyeb's default)
EXPOSE 8080

# Run the application
CMD ["java", "-jar", "target/unble-budget-0.0.1-SNAPSHOT.jar", "--spring.profiles.active=prod"]