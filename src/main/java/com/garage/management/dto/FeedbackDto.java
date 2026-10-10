package com.garage.management.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public class FeedbackDto {
    public record Request(
            @NotNull(message = "Service request is required") Long serviceRequestId,
            @NotNull(message = "Rating is required") @Min(value = 1, message = "Rating must be between 1 and 5") @Max(value = 5, message = "Rating must be between 1 and 5") Integer rating,
            @Size(max = 1000, message = "Comment is too long (max 1000)") String comment) {}

    public record Response(Long id, Long serviceRequestId, String customerName, String serviceName,
                           String vehicleRegistration, int rating, String comment, LocalDateTime createdAt) {}
}
