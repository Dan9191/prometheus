FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY src src

RUN chmod +x ./gradlew
RUN ./gradlew bootJar -Pprod --no-daemon

FROM gcr.io/distroless/java21-debian12

COPY --from=build /app/build/libs/*-SNAPSHOT.jar app.jar

EXPOSE 8066
ENTRYPOINT ["java", "-jar", "/app.jar"]