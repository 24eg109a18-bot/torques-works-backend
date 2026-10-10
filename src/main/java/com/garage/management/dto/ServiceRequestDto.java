package com.garage.management.dto;

import com.garage.management.entity.PaymentStatus;
import com.garage.management.entity.ServiceStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

public class ServiceRequestDto {
    public record AssignRequest(@NotNull(message = "Please select a mechanic") Long mechanicId) {}

    public record StatusRequest(@NotNull(message = "Status is required") ServiceStatus status) {}

    public record WorkRequest(
            @Size(max = 5000, message = "Notes are too long") String inspectionNotes,
            @PositiveOrZero(message = "Labour charge cannot be negative") BigDecimal labourCharge,
            LocalDate estimatedCompletionDate) {}

    public record AddPartRequest(
            @NotNull(message = "Please select a spare part") Long partId,
            @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity) {}

    public record PartLine(Long id, Long partId, String partName, String partNumber, int quantity,
                           BigDecimal unitPrice, BigDecimal lineTotal) {}

    public record Response(Long id, Long appointmentId, Long customerId, String customerName, String customerPhone,
                           Long vehicleId, String vehicleRegistration, String vehicleName,
                           Long serviceId, String serviceName, BigDecimal serviceCost,
                           Long mechanicId, String mechanicName, ServiceStatus status,
                           LocalDate appointmentDate, LocalTime appointmentTime, String problemDescription,
                           String inspectionNotes, BigDecimal labourCharge, LocalDate estimatedCompletionDate,
                           LocalDateTime createdAt, LocalDateTime completedAt,
                           List<PartLine> parts, BigDecimal partsTotal,
                           Long invoiceId, PaymentStatus paymentStatus, boolean feedbackGiven,
                           List<ServiceStatus> allowedNext) {}
}
