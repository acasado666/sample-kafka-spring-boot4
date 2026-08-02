# Stage 1: extract Spring Boot layers for optimal Docker layer caching
FROM eclipse-temurin:25-jre AS builder
WORKDIR /builder
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --launcher --destination extracted

# Stage 2: minimal Alpine runtime (~70% smaller than Ubuntu JRE)
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Layers ordered least → most frequently changed; Docker caches dependencies separately
COPY --from=builder --chown=appuser:appgroup /builder/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /builder/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /builder/extracted/application/ ./

USER appuser
EXPOSE 8080

# -XX:+UseContainerSupport  → JVM reads cgroup limits, not host RAM (prevents heap miscalculation)
# -XX:MaxRAMPercentage=75.0 → heap capped at 75% of container limit; leaves room for off-heap (Kafka/Netty buffers)
ENTRYPOINT ["java", \
    "-XX:+UseContainerSupport", \
    "-XX:MaxRAMPercentage=75.0", \
    "org.springframework.boot.loader.launch.JarLauncher"]

# Build & run:
#   docker build -t order-events-producer:v1 .
#   docker run -p 8080:8080 order-events-producer:v1