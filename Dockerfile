FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests
ENTRYPOINT ["java", "-jar", "target/booking-system-0.0.1-SNAPSHOT.jar"]
