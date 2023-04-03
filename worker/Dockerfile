FROM openjdk:19-slim AS BUILD
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME
COPY build.gradle settings.gradle gradlew $APP_HOME
COPY gradle $APP_HOME/gradle
RUN ./gradlew build || return 0
COPY . .
RUN ./gradlew build

FROM openjdk:19
ENV ARTIFACT_NAME=app.jar
ENV APP_HOME=/usr/app
WORKDIR $APP_HOME
COPY --from=BUILD $APP_HOME/build/libs/* ./$ARTIFACT_NAME
CMD ["sh", "-c", "java -jar $ARTIFACT_NAME"]
