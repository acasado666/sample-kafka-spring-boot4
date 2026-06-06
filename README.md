# Order Events Producer API

A Spring Boot 4 application that provides REST endpoints to publish orders events to Apache Kafka. 
This service enables clients to emit order event changes (ADD, UPDATE, CANCEL) through a well-defined HTTP API.

## 📋 Quick Links

[//]: # (- **[Product Requirements Document]&#40;./docs/PRD.md&#41;** - Complete product specifications, features, and requirements)

[//]: # (- **[Implementation Plan]&#40;./docs/IMPLEMENTATION_PLAN_README.md&#41;** - Engineering roadmap and implementation details)

[//]: # (- **[Architecture Diagram]&#40;./docs/ARCHITECTURE_DIAGRAM.md&#41;** - System architecture, testing setup, and component interactions)

## 🚀 Quick Start

### Prerequisites
- Java 25+
- Docker & Docker Compose (for local Kafka broker) confluentinc/cp-kafka:8.1.3
- Maven

### Setup & Run

1. **Start Kafka Broker** (using Docker Compose):
   ```bash
   docker-compose up -d
   ```
   
   This starts a KRaft-mode Kafka broker on `localhost:9092`


2. **Build the Application**:
   ```bash
   .\mvnw.cmd clean install
   ```

3. **Run the Application**:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
   The API will be available at `http://localhost:8080`


4. **Stop Kafka**:
   ```bash
   docker-compose down
   ```

## 📚 Documentation

[//]: # (### Product Requirements Document &#40;[PRD.md]&#40;./docs/PRD.md&#41;&#41;)
[//]: # (Contains:)

[//]: # (- Product overview and goals)

[//]: # (- Personas and use cases)

[//]: # (- Functional requirements)

[//]: # (- API endpoint specifications)

[//]: # (- Request/response formats)

[//]: # (- Validation rules and error handling)

**Key Points:**
- POST `/api/order-events` - Create new order event (ADD type only)
- PUT `/api/order-events` - Update existing order event (UPDATE type, requires orderEventId)
- DELETE `/api/order-events/{id}` - Cancel existing order event (CANCEL type, requires orderEventId)
- Events are published to Kafka topic: `order-events`

### Implementation Plan 
Covers:
- Domain model design
- API layer implementation
- Kafka producer configuration
- Retry strategy details
- Serialization approach
- Testing strategy
- Observability and logging

[//]: # (### Architecture Diagram &#40;[ARCHITECTURE_DIAGRAM.md]&#40;./docs/ARCHITECTURE_DIAGRAM.md&#41;&#41;)

[//]: # (Includes:)

[//]: # (- Production/Development architecture flow)

[//]: # (- Testing architecture with embedded Kafka)

[//]: # (- System component breakdown)

[//]: # (- Technology stack details)

##  Architecture Overview

### Production/Development Flow
```
REST Client 
    ↓
OrderEventsController (POST/PUT)
    ↓
Validation (Bean Validation)
    ↓
OrderEventProducer (KafkaTemplate)
    ↓
Retry Strategy (at-least-once)
    ↓
Kafka Broker
```

[//]: # (TODO)
[//]: # (### Testing Approach)

[//]: # (- **Integration Tests**: Use `@SpringBootTest` with `@EmbeddedKafka` for real component interactions)

[//]: # (- **No Mocking**: Tests verify actual behavior without mocks)

[//]: # (- **Isolated Environment**: Embedded Kafka provides isolation without external dependencies)

## 🔧 Configuration

### Kafka Configuration (`application.yml`)
```yaml
spring:
   kafka:
      bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
      topic: order-events
      producer:
        key-serializer: org.apache.kafka.common.serialization.IntegerSerializer
        value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

### Docker Compose Setup (`compose.yaml`)
- Runs Kafka 7.4.0 in KRaft mode (no Zookeeper)
- Exposes port 9092 for client connections
- Automatically creates topics

## 📦 Dependencies

### Core
- Spring Boot 4.0.5
- Spring Kafka
- Spring Validation
- Spring Web MVC

### Testing
- Spring Boot Test
- Spring Kafka Test
- JUnit 5
- MockMvc

## 🧪 Testing

[//]: # (TODO)
[//]: # (### Running Tests)

[//]: # (```bash)

[//]: # (./gradlew test)

[//]: # (```)

[//]: # ()
[//]: # (### Integration Tests)

[//]: # (Located in `src/test/java/com/learnkafka/controller/LibOrderraryEventsControllerIntegrationTest.java`)

[//]: # ()
[//]: # (Tests include:)

[//]: # (- Valid POST/PUT operations)

[//]: # (- Validation error scenarios)

[//]: # (- Multiple event publishing)

[//]: # (- Invalid JSON handling)

[//]: # (- Content-Type validation)

[//]: # ()
[//]: # (**Key Test Configuration:**)

[//]: # (```java)

[//]: # (@SpringBootTest&#40;webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT&#41;)

[//]: # (@AutoConfigureMockMvc)

[//]: # (@EmbeddedKafka&#40;partitions = 1, topics = "order-events"&#41;)

[//]: # (```)

## 📝 API Examples

### Create Order Event (POST)
```bash
curl -X POST http://localhost:8080/api/order-events \
  -H "Content-Type: application/json" \
  -d '{
#  
  }'
```

**Response (201 Created):**
```json
{

}
```

### Update Order Event (PUT)
```bash
curl -X PUT http://localhost:8080/api/order-events \
  -H "Content-Type: application/json" \
  -d '{
    
  }'
```

## 📂 Project Structure

```
order-events-producer/
├── src/
│   ├── main/
│   │   ├── java/com/kopdebytes/acasado/
│   │   │   ├── controller/      # API endpoints
│   │   │   ├── domain/          # Domain models
│   │   │   ├── producer/        # Kafka producer
│   │   │   └── ...
│   │   └── resources/
│   │       └── application.yml   # Configuration
│   └── test/
│       ├── java/com/kopdebytes/acasado/
│       │   └── controller/       # Integration tests
│       └── resources/
├── docs/
│   ├── PRD.md                    # Product requirements
│   ├── IMPLEMENTATION_PLAN_README.md
│   └── ARCHITECTURE_DIAGRAM.md
├── compose.yaml                  # Docker Compose Kafka setup
├── pom.xml                       # Maven configuration
└── README.md                     # This file
```

## 🔍 Key Concepts

### Event Types
- **ADD**: Create a new order event (POST endpoint)
- **UPDATE**: Update an existing order event (PUT endpoint)
- **DELETE**: Delete an existing order event (DELETE endpoint)

### Validation Rules
- `orderEventType` is required and must be valid
- For POST: `orderEventType` must be ADD
- For PUT: `orderEventType` is required, `orderEventType` must be UPDATE
- Phone fields (`phoneId`, `phoneName`, `phoneModel`, `phonePrice`, `phoneManufacturer`) are required and cannot be blank

### Kafka Publishing
- Messages are published to `order-events` topic
- Uses `orderId` as message key (when present)
- JSON serialization for payloads
- At-least-once delivery semantics with retry strategy

## 🛠️ Development

### Building
```bash
.\mvnw.cmd clean install
```
### Running
```bash
.\mvnw.cmd spring-boot:run
```
### Development Mode
The `spring-boot-docker-compose` dependency automatically starts Docker Compose services when running the application:
```bash
.\mvnw.cmd spring-boot:run
```

### Debugging
Enable debug logging in `application.yml`:
```yaml
logging:
  level:
    com.learnkafka: DEBUG
    org.springframework.kafka: DEBUG
```

## 📊 Monitoring & Observability

The application logs:
- Request receipt and validation
- Kafka publish success/failure
- Retry attempts and final failures
- Error details with stack traces

## 🚨 Error Handling

| Status Code | Scenario |
|-------------|----------|
| 201 Created | Event published successfully (POST) |
| 200 OK | Event published successfully (PUT) |
| 400 Bad Request | Validation error (invalid payload, missing fields) |
| 415 Unsupported Media Type | Missing/invalid Content-Type header |
| 500 Internal Server Error | Kafka publish failure after retries |

## 📞 Support & Contributions

[//]: # (TODO)

## 📄 License

This is an educational project for learning Kafka and Spring Boot integration.

---

**Last Updated**: March 2026  
**Version**: 0.0.1-SNAPSHOT

