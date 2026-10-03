FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw
COPY src/ src/
RUN ./mvnw -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S deployforge && adduser -S deployforge -G deployforge
WORKDIR /app
COPY --from=build --chown=deployforge:deployforge /build/target/deployforge-0.0.1-SNAPSHOT.jar app.jar
USER deployforge
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
