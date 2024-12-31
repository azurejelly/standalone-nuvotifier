FROM gradle:8.10.0-jdk17 AS build

WORKDIR /build

COPY gradlew gradlew
COPY gradle/ gradle/
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    ./gradlew --no-daemon build -x test || true

COPY . .

RUN --mount=type=cache,target=/home/gradle/.gradle/caches \
    ./gradlew --no-daemon nuvotifier-bukkit:build

FROM eclipse-temurin:21-jre-alpine

WORKDIR /srv

COPY .devenv/utils/download.sh .

RUN apk update && apk add curl bash jq nano
RUN chmod +x ./download.sh && \
    mkdir -p ./plugins/NuVotifier/ && \
    echo eula=true > ./eula.txt

RUN ./download.sh

COPY --from=build "/build/bukkit/build/libs/nuvotifier-bukkit-*-dist.jar" ./plugins/nuvotifier-bukkit.jar

CMD [ "java", "-jar", "./paper.jar" ]