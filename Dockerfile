# Copy and paste this entire command:
@"
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY . .
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests
ENTRYPOINT [\"java\", \"-jar\", \"target/*.jar\"]
"@ | Out-File -FilePath Dockerfile -Encoding UTF8 -Force