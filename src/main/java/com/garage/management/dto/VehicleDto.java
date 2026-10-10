package com.garage.management.dto;

import com.garage.management.util.Patterns;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class VehicleDto {
    public record Request(
            @NotBlank(message = "Registration number is required")
            @Pattern(regexp = Patterns.REG_NO, message = "Enter a valid registration number, e.g. TS09EB1234") String registrationNumber,
            @NotBlank(message = "Brand is required") String brand,
            @NotBlank(message = "Model is required") String model,
            String variant,
            @NotNull(message = "Manufacturing year is required")
            @Min(value = 1980, message = "Manufacturing year must be 1980 or later")
            @Max(value = 2100, message = "Enter a valid manufacturing year") Integer manufacturingYear,
            @NotBlank(message = "Fuel type is required") String fuelType,
            @NotNull(message = "Current kilometres is required")
            @PositiveOrZero(message = "Kilometres cannot be negative") Integer currentKm,
            String color,
            LocalDate insuranceExpiry,
            String rcNumber) {}

    public record Response(Long id, String registrationNumber, String brand, String model, String variant,
                           Integer manufacturingYear, String fuelType, Integer currentKm, String color,
                           LocalDate insuranceExpiry, String rcNumber, Long ownerId, String ownerName) {}
}
