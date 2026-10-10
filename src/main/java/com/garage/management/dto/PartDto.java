package com.garage.management.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class PartDto {
    public record Request(
            @NotBlank(message = "Part name is required") String name,
            @NotBlank(message = "Part number is required") String partNumber,
            String brand,
            String category,
            @NotNull(message = "Price is required") @Positive(message = "Price must be greater than 0") BigDecimal price,
            @NotNull(message = "Quantity is required") @PositiveOrZero(message = "Quantity cannot be negative") Integer quantity,
            @NotNull(message = "Minimum stock level is required") @PositiveOrZero(message = "Minimum stock cannot be negative") Integer minStockLevel) {}

    public record Response(Long id, String name, String partNumber, String brand, String category,
                           BigDecimal price, int quantity, int minStockLevel, boolean lowStock) {}
}
