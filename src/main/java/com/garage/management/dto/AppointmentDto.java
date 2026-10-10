package com.garage.management.dto;

import com.garage.management.entity.ServiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class AppointmentDto {
    public record BookRequest(
            @NotNull(message = "Please select a vehicle") Long vehicleId,
            @NotNull(message = "Please select a service") Long serviceId,
            @NotNull(message = "Appointment date is required") LocalDate date,
            @NotNull(message = "Appointment time is required") LocalTime time,
            @NotBlank(message = "Please describe the problem") @Size(max = 1000, message = "Problem description is too long (max 1000)") String problemDescription) {}

    public record StatusRequest(@NotNull(message = "Status is required") ServiceStatus status) {}

    public record Response(Long id, Long customerId, String customerName, Long vehicleId, String vehicleRegistration,
                           String vehicleName, Long serviceId, String serviceName, LocalDate date, LocalTime time,
                           String problemDescription, ServiceStatus status, LocalDateTime createdAt,
                           Long serviceRequestId, String mechanicName) {}
}
