package com.kodebytes.acasado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PhoneDto(
        @NotNull(message = "phoneId is required")
        Long phoneId,

        @NotBlank(message = "phoneName is required")
        String phoneName,

        @NotBlank(message = "phoneModel is required")
        String phoneModel,

        @NotNull(message = "phonePrice is required")
        Long phonePrice,

        @NotBlank(message = "phoneManufacturer is required")
        String phoneManufacturer
) {
}
