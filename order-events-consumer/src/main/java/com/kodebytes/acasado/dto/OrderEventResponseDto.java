package com.kodebytes.acasado.dto;

import com.kodebytes.acasado.domain.OrderEventType;

import java.time.LocalDateTime;

public record OrderEventResponseDto(
        Integer orderEventId,
        OrderEventType eventType,
        PhoneResponseDto phone,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
