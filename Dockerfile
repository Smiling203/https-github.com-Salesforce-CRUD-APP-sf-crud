FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
COPY src src

RUN chmod +x mvnw && ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=build /app/target/sf-crud-0.0.1-SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]