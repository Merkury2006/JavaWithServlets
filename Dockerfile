FROM gradle:8.5-jdk21 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle installDist -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/install/JavaWithServlets /app/
RUN chmod +x bin/JavaWithServlets

ENTRYPOINT ["bin/JavaWithServlets"]