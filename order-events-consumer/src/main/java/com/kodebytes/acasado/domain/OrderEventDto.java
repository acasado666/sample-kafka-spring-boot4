package com.kodebytes.acasado.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OrderEventDto(
        @NotNull(message = "orderId is required")
        Long orderEventId,

        @NotNull
        OrderEventType eventType,

        @Valid
        @NotNull
        Phone phone
) {
}
