package com.kodebytes.acasado.dto;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record PhoneResponseDto(
        @NotNull(message = "phoneId is required")
        Long phoneId,

        @NotNull(message = "phoneName is required")
        String phoneName,

        @NotNull(message = "phoneModel is required")
        String phoneModel,

        @NotNull(message = "phoneManufacturer is required")
        String phoneManufacturer,

        @NotNull(message = "phonePrice is required")
        Long phonePrice,

        @NotNull(message = "orderId is required")
        Long orderId,

        @NotNull(message = "createdAt is required")
        Instant createdAt,

        @NotNull(message = "updatedAt is required")
        Instant updatedAt
) {
}
