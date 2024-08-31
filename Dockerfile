FROM gradle:8.10.0-jdk17 AS build

WORKDIR /build

COPY . .

RUN ./gradlew nuvotifier-standalone:build

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /build/standalone/build/libs/nuvotifier-standalone.jar .

EXPOSE 8192

CMD ["java", "-jar", "nuvotifier-standalone.jar"]