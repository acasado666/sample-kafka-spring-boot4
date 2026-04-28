package com.kodebytes.acasado.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record OrderEventResponseDto(
        @NotNull(message = "orderId is required")
        Long orderId,

        @NotBlank(message = "eventType is required")
        OrderEventType eventType,

        @Valid
        @NotNull(message = "phone is required")
        PhoneResponseDto phone,

        @NotNull(message = "createdAt is required")
        Instant createdAt,

        @NotNull(message = "updatedAt is required")
        Instant updatedAt
) {
}
