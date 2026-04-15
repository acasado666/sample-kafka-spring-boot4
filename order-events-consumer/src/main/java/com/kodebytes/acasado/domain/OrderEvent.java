package com.kodebytes.acasado.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OrderEvent(
        Long orderId,

        @NotNull(message = "eventType is required")
        OrderEventType eventType,

        @Valid
        @NotNull(message = "phone is required")
        Phone phone

) {
}
