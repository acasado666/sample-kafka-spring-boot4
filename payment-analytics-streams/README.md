# Payment Analytics Streams

Spring Boot 4 and Kafka Streams service for payment enrichment, deterministic fraud detection, and five-minute account metrics.

## Topics

- `payments.v1`: payments keyed by payment ID
- `merchant-risks.v1`: compacted merchant reference data
- `enriched-payments.v1`: all valid enriched payments
- `fraud-alerts.v1`: flagged payments
- `account-payment-metrics.v1`: windowed account totals

The topology uses `exactly_once_v2`. Production needs at least three brokers and replication factor 3; local development uses replication factor 1.

## Build and test

```powershell
cd payment-analytics-streams
..\mvnw.cmd clean verify
```

## Run

```powershell
docker compose -f docker-compose-payment-analytics.yml up -d
..\mvnw.cmd spring-boot:run
```

Health: `http://localhost:8082/actuator/health`  
Prometheus: `http://localhost:8082/actuator/prometheus`

See [the contracts and operating guide](../docs/PAYMENT_ANALYTICS_STREAMS.md).
