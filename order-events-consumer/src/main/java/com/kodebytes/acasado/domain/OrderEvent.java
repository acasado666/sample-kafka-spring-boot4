package com.kodebytes.acasado.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OrderEvent(
        Integer orderId,

        @NotNull
        OrderEventType eventType,

        @Valid
        @NotNull
        Phone phone
) {
}
