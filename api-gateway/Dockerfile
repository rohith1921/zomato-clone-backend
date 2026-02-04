# 1. Use a lightweight Java 21 image
FROM eclipse-temurin:21-jre-alpine

# 2. Set working folder
WORKDIR /app

# 3. Copy the built JAR file
COPY target/*.jar app.jar

# 4. Expose the API port
EXPOSE 8080

# 5. Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]