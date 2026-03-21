# Stage 1: Build the Maven Project
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY . .
# This builds your .jar file and skips tests to save time
RUN mvn clean package -DskipTests

# Stage 2: Final Run Image (Java + Node.js)
FROM openjdk:17-jdk-slim
WORKDIR /app

# Install Node.js so your backend can run JS files
RUN apt-get update && apt-get install -y curl && \
    curl -sL https://deb.nodesource.com/setup_18.x | bash - && \
    apt-get install -y nodejs

# Copy the compiled JAR from the build stage
COPY --from=build /app/target/*.jar app.jar
# Copy the rest of your files (like server.js and package.json)
COPY . .
RUN npm install

# Render requires your app to listen on port 10000
EXPOSE 10000

# Start command: This runs your Node server
CMD ["node", "server.js"]