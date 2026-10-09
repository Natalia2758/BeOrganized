# STAGE 1: build an application into jar file
FROM maven:3-eclipse-temurin-17 AS build
WORKDIR /app
COPY  . .
RUN mvn clean package -DskipTests

# STAGE 2: copy jar from build stage, listen on port 8081
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]