package com.kodebytes.acasado.config;

import com.kodebytes.acasado.domain.OrderEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodebytes.acasado.entity.OrderEventFailure;
import com.kodebytes.acasado.repository.OrderEventFailureRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.*;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;

import java.io.PrintWriter;
import java.io.StringWriter;

@Configuration
@EnableKafka
public class OrderEventsConsumerConfig {

    private static final Logger log = LoggerFactory.getLogger(OrderEventsConsumerConfig.class);
    private static final String DLT_TOPIC = "order-events.DLT";
    private static final String TOPIC = "order-events";

    @Value("${app.kafka.recovery.mode:failure-table}")
    private String recoveryMode;

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

//    public OrderEventsConsumerConfig(
//            FailureRecordService failureRecordService,
//            @Value("${app.kafka.recovery.mode:failure-table}") String recoveryMode,
//            @Value("${spring.kafka.consumer.bootstrap-servers:localhost:9092}") String bootstrapServers) {
//        this.failureRecordService = failureRecordService;
//        this.recoveryMode = recoveryMode;
//        this.bootstrapServers = bootstrapServers;
//    }

    // ── Container Factory ────────────────────────────────────────────────────
    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<Integer, OrderEventDto>> kafkaListenerContainerFactory(
            ConsumerFactory<Integer, OrderEventDto> consumerFactory,
            DefaultErrorHandler errorHandler) {

        var factory = new ConcurrentKafkaListenerContainerFactory<Integer, OrderEventDto>();
        factory.setConsumerFactory(consumerFactory);
        factory.setCommonErrorHandler(errorHandler);
        // factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL); // Default ContainerProperties.AckMode.BATCH
        // factory.setConcurrency(3);  // ← 3 consumer threads
        return factory;
    }

    @Bean
    DefaultErrorHandler defaultErrorHandler(KafkaTemplate<Integer, Object> kafkaTemplate,
                                            OrderEventFailureRepository orderEventFailureRepository,
                                            ObjectMapper objectMapper) {
        var exponentialBackOff = new ExponentialBackOffWithMaxRetries(2);
        exponentialBackOff.setInitialInterval(1000L);
        exponentialBackOff.setMultiplier(2.0);
        exponentialBackOff.setMaxInterval(4000L);

        String mode = recoveryMode == null ? "" : recoveryMode.trim().toLowerCase();
        DefaultErrorHandler errorHandler = switch (mode) {
            case "dlt" -> {
                var recoverer = new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) -> resolveDltPartition(record, kafkaTemplate)
                );
                log.info("Kafka recovery mode is '{}'; failed records will be published to topic '{}'.", recoveryMode, DLT_TOPIC);
                yield new DefaultErrorHandler(recoverer, exponentialBackOff);
            }
            case "failure-table" -> {
                ConsumerRecordRecoverer failureTableRecoverer = (record, exception) -> {
                    var failure = new OrderEventFailure();
                    failure.setTopic(record.topic());
                    failure.setPartitionId(record.partition());
                    failure.setOffsetValue(record.offset());
                    failure.setRecordKey(record.key() != null ? record.key().toString() : null);
                    failure.setPayload(toPayload(record, objectMapper));
                    failure.setExceptionClass(exception.getClass().getName());
                    failure.setExceptionMessage(exception.getMessage());
                    failure.setStackTrace(toStackTrace(exception));
                    orderEventFailureRepository.save(failure);
                    log.error("Recovery: persisted failed record to table. topic={}, partition={}, offset={}, exceptionType={}",
                            record.topic(), record.partition(), record.offset(), exception.getClass().getSimpleName(), exception);
                };
                log.info("Kafka recovery mode is '{}'; failed records will be persisted to the failure table.", recoveryMode);
                yield new DefaultErrorHandler(failureTableRecoverer, exponentialBackOff);
            }
            case "both" -> {
                var dltRecoverer = new DeadLetterPublishingRecoverer(
                        kafkaTemplate,
                        (record, exception) -> resolveDltPartition(record, kafkaTemplate)
                );
                ConsumerRecordRecoverer bothRecoverer = (record, exception) -> {
                    // 1. Publish to DLT
                    try {
                        dltRecoverer.accept(record, exception);
                    } catch (Exception e) {
                        log.error("Failed to publish to DLT for record. topic={}, partition={}, offset={}, exceptionType={}. Exception: {}",
                                record.topic(), record.partition(), record.offset(), exception.getClass().getSimpleName(), e.getMessage(), e);
                    }

                    // 2. Persist to failure table
                    var failure = new OrderEventFailure();
                    failure.setTopic(record.topic());
                    failure.setPartitionId(record.partition());
                    failure.setOffsetValue(record.offset());
                    failure.setRecordKey(record.key() != null ? record.key().toString() : null);
                    failure.setPayload(toPayload(record, objectMapper));
                    failure.setExceptionClass(exception.getClass().getName());
                    failure.setExceptionMessage(exception.getMessage());
                    failure.setStackTrace(toStackTrace(exception));
                    orderEventFailureRepository.save(failure);
                    log.error("Recovery (both): published to DLT '{}' and persisted to failure table. topic={}, partition={}, offset={}, exceptionType={}",
                            DLT_TOPIC, record.topic(), record.partition(), record.offset(), exception.getClass().getSimpleName(), exception);
                };
                log.info("Kafka recovery mode is '{}'; failed records will be published to DLT '{}' AND persisted to the failure table.", recoveryMode, DLT_TOPIC);
                yield new DefaultErrorHandler(bothRecoverer, exponentialBackOff);
            }
            case "log_skip" -> {
                ConsumerRecordRecoverer logAndSkip = (record, exception) -> {
                    log.error("Recovery: skipping failed record. Topic={}, Partition={}, Offset={}, Exception={}",
                            record.topic(), record.partition(), record.offset(), exception.getMessage(), exception);
                };
                yield new DefaultErrorHandler(logAndSkip, exponentialBackOff);
            }
            default -> {
                log.info("Kafka recovery mode is '{}'; DLT publish is disabled.", recoveryMode);
                yield new DefaultErrorHandler(exponentialBackOff);
            }
        };

        RetryListener retryListener = new RetryListener() {
            @Override
            public void failedDelivery(ConsumerRecord<?, ?> record,
                                       Exception ex,
                                       int deliveryAttempt) {
                var message = String.format(
                        "Retry attempt %d failed for topic=%s partition=%d offset=%d: %s",
                        deliveryAttempt,
                        record.topic(),
                        record.partition(),
                        record.offset(),
                        ex.getMessage());
                log.warn(message, ex);
            }
        };
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,        // bad payload — will never succeed
                NullPointerException.class,            // programming error
                DataIntegrityViolationException.class  // duplicate key — will always fail
        );
        errorHandler.setRetryListeners(retryListener);

        return errorHandler;
    }

    private TopicPartition resolveDltPartition(ConsumerRecord<?, ?> record, KafkaTemplate<Integer, Object> kafkaTemplate) {
        try {
            var partitions = kafkaTemplate.partitionsFor(DLT_TOPIC);
            int partitionCount = partitions == null ? 0 : partitions.size();
            int partition = partitionCount > 0 ? Math.min(record.partition(), partitionCount - 1) : 0;
            return new TopicPartition(DLT_TOPIC, partition);
        } catch (Exception ex) {
            log.warn("Unable to resolve partition metadata for DLT topic '{}'; defaulting to partition 0.", DLT_TOPIC, ex);
            return new TopicPartition(DLT_TOPIC, 0);
        }
    }

    private String toPayload(ConsumerRecord<?, ?> record, ObjectMapper objectMapper) {
        try {
            return objectMapper.writeValueAsString(record.value());
        } catch (Exception serializationException) {
            log.warn("Failed to serialize consumer record payload. Falling back to String payload representation.", serializationException);
            return String.valueOf(record.value());
        }
    }

    private String toStackTrace(Exception exception) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }
//    @Bean
//    public ProducerFactory<Integer, Object> dltProducerFactory() {
//        Map<String, Object> props = new HashMap<>();
//        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
//        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
//        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
//        return new DefaultKafkaProducerFactory<>(props);
//    }
//
//    @Bean
//    public KafkaTemplate<Integer, Object> dltKafkaTemplate(ProducerFactory<Integer, Object> dltProducerFactory) {
//        return new KafkaTemplate<>(dltProducerFactory);
//    }
//
//    @Bean
//    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<Integer, Object> dltKafkaTemplate) {
//        return new DeadLetterPublishingRecoverer(
//                dltKafkaTemplate,
//                (record, ex) -> new TopicPartition(record.topic() + ".DLT", record.partition())
//        );
//    }
//
//    @Bean
//    public ConsumerRecordRecoverer consumerRecordRecoverer(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
//        RecoveryMode mode = RecoveryMode.from(recoveryMode);
//        log.info("Kafka recovery mode: {}", mode);
//
//        return (record, exception) -> {
//            switch (mode) {
//                case DLT -> publishToDlt(record, exception, deadLetterPublishingRecoverer);
//                case BOTH -> {
//                    persistFailureRecord(record, exception);
//                    publishToDlt(record, exception, deadLetterPublishingRecoverer);
//                }
//                case FAILURE_TABLE -> persistFailureRecord(record, exception);
//            }
//        };
//    }

    // ── Error Handler ────────────────────────────────────────────────────────
    // Retries up to 3 times with 1-second fixed backoff.
    // On exhaustion, recovery strategy is selected by app.kafka.recovery.mode
    // (failure-table | dlt | both).

//    @Bean
//    public DefaultErrorHandler errorHandler(ConsumerRecordRecoverer consumerRecordRecoverer) {
//
//        // Retry 3 times, wait 1 second between attempts
//        var fixedBackOff = new FixedBackOff(1_000L, 3L);
//
//        var errorHandler = new DefaultErrorHandler(consumerRecordRecoverer, fixedBackOff);
//
//        // These exceptions skip retries and go straight to the recoverer
//        errorHandler.addNotRetryableExceptions(
//                DeserializationException.class,         // malformed JSON / type mismatch
//                IllegalArgumentException.class,          // bad payload — will never succeed
//                DataIntegrityViolationException.class    // duplicate key — always fails
//        );
//
//        // Consumer error listener — covers the full retry lifecycle
//        errorHandler.setRetryListeners(new RetryListener() {
//
//            @Override
//            public void failedDelivery(ConsumerRecord<?, ?> record, Exception ex, int deliveryAttempt) {
//                log.warn("Delivery attempt {} failed. Topic={}, Partition={}, Offset={}, Error={}",
//                        deliveryAttempt,
//                        record.topic(), record.partition(), record.offset(),
//                        ex.getMessage());
//            }
//
//            @Override
//            public void recovered(ConsumerRecord<?, ?> record, Exception ex) {
//                log.info("Record recovered after retries. Topic={}, Partition={}, Offset={}",
//                        record.topic(), record.partition(), record.offset());
//            }
//
//            @Override
//            public void recoveryFailed(ConsumerRecord<?, ?> record, Exception original, Exception failure) {
//                log.error("Record recovery failed. Topic={}, Partition={}, Offset={}, OriginalError={}, RecoveryError={}",
//                        record.topic(), record.partition(), record.offset(),
//                        original.getMessage(), failure.getMessage());
//            }
//        });
//
//        return errorHandler;
//    }
//
//
//
//    private void persistFailureRecord(ConsumerRecord<?, ?> record, Exception exception) {
//        log.error("All retries exhausted. Persisting failed record to failure_record table. "
//                        + "Topic={}, Partition={}, Offset={}, Exception={}",
//                record.topic(), record.partition(), record.offset(), exception.getMessage());
//
//        //noinspection unchecked
//        failureRecordService.saveFailureRecord(
//                (ConsumerRecord<Integer, OrderEventDto>) record,
//                exception
//        );
//    }
//
//    private void publishToDlt(
//            ConsumerRecord<?, ?> record,
//            Exception exception,
//            DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
//        log.error("All retries exhausted. Publishing failed record to DLT. Topic={}, Partition={}, Offset={}, Exception={}",
//                record.topic(), record.partition(), record.offset(), exception.getMessage());
//        deadLetterPublishingRecoverer.accept(record, exception);
//    }
//
//    private enum RecoveryMode {
//        FAILURE_TABLE,
//        DLT,
//        BOTH;
//
//        private static RecoveryMode from(String value) {
//            String normalized = value == null ? "" : value.trim().toUpperCase().replace('-', '_');
//            return switch (normalized) {
//                case "FAILURE_TABLE" -> FAILURE_TABLE;
//                case "DLT" -> DLT;
//                case "BOTH" -> BOTH;
//                default -> throw new IllegalArgumentException(
//                        "Invalid app.kafka.recovery.mode: " + value + " (expected: failure-table, dlt, both)");
//            };
//        }
//    }
}
