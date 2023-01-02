FROM openjdk:11-jdk-slim
COPY . /app
WORKDIR /app
RUN ./gradlew build

FROM openjdk:11-jdk-slim
COPY --from=0 /app/build/libs/*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
