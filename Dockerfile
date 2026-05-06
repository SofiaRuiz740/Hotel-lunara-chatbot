FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

COPY pom.xml ./
COPY mvnw ./
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080

ENTRYPOINT ["java", "-Dserver.port=${PORT}", "-jar", "app.jar"]