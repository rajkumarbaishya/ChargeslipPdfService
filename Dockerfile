# Build stage
FROM maven:3.9-amazoncorretto-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM amazoncorretto:17-alpine
WORKDIR /app
COPY --from=builder /app/target/Baas_chargeslip-pdf-service-0.0.1-SNAPSHOT.jar app.jar

# Install curl
RUN apk update && apk add curl

EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
