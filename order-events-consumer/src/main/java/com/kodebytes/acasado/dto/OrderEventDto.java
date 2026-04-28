package com.kodebytes.acasado.dto;

import com.kodebytes.acasado.domain.OrderEventType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OrderEventDto(
        @NotNull(message = "orderId is required")
        Long orderId,

        @NotNull
        OrderEventType eventType,

        @Valid
        @NotNull
        PhoneDto phone
) {
}
