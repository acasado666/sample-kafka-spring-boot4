FROM eclipse-temurin:25-jre
WORKDIR /app
RUN useradd --system --uid 10001 spring
COPY target/payment-analytics-streams-0.1.0-SNAPSHOT.jar app.jar
USER 10001
EXPOSE 8082
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]
