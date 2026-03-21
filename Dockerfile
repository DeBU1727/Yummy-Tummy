# Stage 1: Build the Maven Project
FROM maven:3.8.5-eclipse-temurin-17 AS build
WORKDIR /app
COPY . .
# Builds the JAR file
RUN mvn clean package -DskipTests

# Stage 2: Final Run Image
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app

# Copy only the compiled JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Render requires your app to listen on port 10000
ENV PORT=10000
EXPOSE 10000

# Start the Java application
ENTRYPOINT ["java", "-jar", "app.jar"]