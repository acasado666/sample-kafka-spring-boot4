package com.kodebytes.acasado.domain;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record OrderEventDto(
        Long orderEventId,

        @NotNull
        OrderEventType eventType,

        @Valid
        @NotNull
        Phone phone
) {
}
