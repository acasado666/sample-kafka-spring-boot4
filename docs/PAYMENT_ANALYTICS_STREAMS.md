# Payment analytics and fraud-enrichment guide

The service joins versioned JSON payments with a compacted merchant-risk table. Deterministic rules flag payments of CHF 10,000 or more, blocked merchants, and merchant risk scores of 80 or more. Kafka Streams commits outputs with `exactly_once_v2`.

## Contracts and topics

`payments.v1` is keyed by `paymentId`. Required processing fields are `accountId` and `amount`. Producers should provide `schemaVersion`, `merchantId`, ISO-4217 currency, country, and ISO-8601 `occurredAt`. `merchant-risks.v1` is keyed by merchant ID and uses compaction. Breaking JSON changes require a new topic version.

| Topic | Partitions | Replication | Cleanup |
|---|---:|---:|---|
| payments.v1 | 12 | 3 | delete |
| merchant-risks.v1 | 12 | 3 | compact |
| enriched-payments.v1 | 12 | 3 | delete |
| fraud-alerts.v1 | 12 | 3 | delete |
| account-payment-metrics.v1 | 12 | 3 | compact,delete |

Use `min.insync.replicas=2`, equal partition counts for joined topics, and disabled auto-topic creation in production.

## Operations

Scale instances up to the partition count. Mount the Streams state directory on fast storage when restoration time matters. Scrape `/actuator/prometheus`; alert on thread state, record lateness, dropped records, restore latency, commit failures, and lag. Use readiness and liveness Actuator probes.

A breaking state schema change requires a new application ID or planned state migration. Changing the application ID reprocesses retained input. Invalid JSON is logged and skipped; valid events without account ID or amount are filtered. Payment IDs must remain globally unique for reconciliation.
