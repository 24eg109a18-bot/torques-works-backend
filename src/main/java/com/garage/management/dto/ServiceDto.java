package com.garage.management.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class ServiceDto {
    public record Request(
            @NotBlank(message = "Service name is required") @Size(max = 100) String name,
            @Size(max = 500, message = "Description is too long (max 500)") String description,
            @NotNull(message = "Estimated cost is required") @Positive(message = "Estimated cost must be greater than 0") BigDecimal estimatedCost,
            @NotNull(message = "Estimated duration is required") @Positive(message = "Duration must be at least 1 hour") Integer estimatedDurationHours,
            Boolean active) {}

    public record Response(Long id, String name, String description, BigDecimal estimatedCost,
                           Integer estimatedDurationHours, boolean active) {}
}
