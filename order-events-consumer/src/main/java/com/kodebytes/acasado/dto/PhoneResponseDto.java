package com.kodebytes.acasado.dto;

import java.time.Instant;

public record PhoneResponseDto(
            Long phoneId,
            String phoneName,
            String phoneModel,
            String phoneManufacturer,
            Long phonePrice,
            Long orderId,
            Instant createdAt,
            Instant updatedAt
) {
}
