FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
