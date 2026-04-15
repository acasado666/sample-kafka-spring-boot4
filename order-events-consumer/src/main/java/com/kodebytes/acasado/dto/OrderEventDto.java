package com.kodebytes.acasado.dto;

import com.kodebytes.acasado.domain.OrderEventType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OrderEventDto(
        Long orderId,

        @NotNull
        OrderEventType eventType,

        @Valid
        @NotNull
        PhoneDto phone
) {
}
