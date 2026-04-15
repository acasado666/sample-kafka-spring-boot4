package com.kodebytes.acasado.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record OrderEventResponseDto(
        Long orderId,
        OrderEventType eventType,
        PhoneResponseDto phone,
        Instant createdAt,
        Instant updatedAt
) {
}
