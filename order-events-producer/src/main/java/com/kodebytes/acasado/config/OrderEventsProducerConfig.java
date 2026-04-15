package com.kodebytes.acasado.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application-level producer configuration.
 *
 * <p>Explicitly defines an {@link ObjectMapper} bean so it is available
 * for injection into {@code OrderEventProducer} (StringSerializer mode).
 */
@Configuration
public class OrderEventsProducerConfig {

    /**
     * Bean {@link ObjectMapper} used by the Kafka OrderEventProducer to manually
     * serialize {@code OrderEvent} to a JSON string (StringSerializer mode).
     *
     * <ul>
     *   <li>{@code WRITE_DATES_AS_TIMESTAMPS} disabled — ISO-8601 date as readable strings.</li>
     *   <li>{@code FAIL_ON_EMPTY_BEANS} disabled — safe default for records/POJOs.</li>
     * </ul>
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }
}

