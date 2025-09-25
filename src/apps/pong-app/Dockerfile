########################
# Build
########################
FROM eclipse-temurin:21-jdk AS build
WORKDIR /src

COPY gradlew ./
COPY gradle gradle
COPY settings.gradle.kts build.gradle.kts gradle.properties ./
RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle/wrapper \
	--mount=type=cache,target=/root/.gradle/caches \
	./gradlew --no-daemon -q help

COPY src ./src
RUN --mount=type=cache,target=/root/.gradle/wrapper \
	--mount=type=cache,target=/root/.gradle/caches \
	./gradlew --no-daemon clean shadowJar

########################
# Run
########################
FROM eclipse-temurin:21-jre-jammy

ENV APP_HOME=/app \
	JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"
WORKDIR $APP_HOME

COPY --from=build /src/build/libs/*-all.jar ./app.jar

RUN useradd -r -u 10001 appuser && chown -R appuser:appuser $APP_HOME
USER 10001

EXPOSE 8082
ENTRYPOINT ["java","-jar","/app/app.jar"]