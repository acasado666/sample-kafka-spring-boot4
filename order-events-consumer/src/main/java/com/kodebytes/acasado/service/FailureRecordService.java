package com.kodebytes.acasado.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodebytes.acasado.domain.FailureRecord;
import com.kodebytes.acasado.dto.OrderEventDto;
import com.kodebytes.acasado.repository.FailureRecordRepository;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FailureRecordService {

    private static final Logger log = LoggerFactory.getLogger(FailureRecordService.class);

    public static final String OPEN = "OPEN";
    public static final String FIXED = "FIXED";

    private final FailureRecordRepository failureRecordRepository;
    private final OrderEventService orderEventService;
    private final ObjectMapper objectMapper;

    public FailureRecordService(FailureRecordRepository failureRecordRepository,
                                OrderEventService orderEventService,
                                ObjectMapper objectMapper) {
        this.failureRecordRepository = failureRecordRepository;
        this.orderEventService = orderEventService;
        this.objectMapper = objectMapper;
    }

    public void saveFailureRecord(ConsumerRecord<Integer, OrderEventDto> record, Exception exception) {
        String serializedValue;
        try {
            serializedValue = objectMapper.writeValueAsString(record.value());
        } catch (JsonProcessingException e) {
            serializedValue = record.value() != null ? record.value().toString() : "null";
        }

        FailureRecord failureRecord = new FailureRecord(
                record.topic(),
                record.key(),
                serializedValue,
                record.partition(),
                record.offset(),
                exception.getMessage(),
                OPEN
        );

        failureRecordRepository.save(failureRecord);
        log.info("Saved failure record: {}", failureRecord);
    }

    public void retryFailedRecords() {
        List<FailureRecord> openRecords = failureRecordRepository.findAllByStatus(OPEN);
        log.info("Found {} OPEN failure records to retry", openRecords.size());

        openRecords.forEach(failureRecord -> {
            try {
                OrderEventDto orderEventDto = objectMapper.readValue(
                        failureRecord.getErrorRecord(), OrderEventDto.class);
                // TODO: consider adding headers or other metadata from failureRecord if needed
                // TODO: could be Long not Integer
                ConsumerRecord<Integer, OrderEventDto> consumerRecord =
                        new ConsumerRecord<>(
                                failureRecord.getTopic(),
                                failureRecord.getPartition(),
                                failureRecord.getOffsetValue(),
                                failureRecord.getKeyValue(),
                                orderEventDto
                        );

                orderEventService.processEvent(consumerRecord);

                failureRecord.setStatus(FIXED);
                failureRecordRepository.save(failureRecord);
                log.info("Successfully retried failure record id={}, marked as FIXED", failureRecord.getId());

            } catch (Exception e) {
                log.error("Retry failed for failure record id={}: {}", failureRecord.getId(), e.getMessage());
            }
        });
    }
}
