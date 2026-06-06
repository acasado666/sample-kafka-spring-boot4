package com.kodebytes.acasado.dto;

import java.time.LocalDateTime;

public record PhoneResponseDto(
        Integer phoneId,
        String phoneName,
        String phoneModel,
        String phoneManufacturer,
        Long phonePrice,
        Integer orderId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
