package com.garage.management.dto;

import com.garage.management.entity.PaymentMethod;
import com.garage.management.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class InvoiceDto {
    public record CreateRequest(@NotNull(message = "Service request is required") Long serviceRequestId) {}

    public record Response(Long id, String invoiceNumber, LocalDate invoiceDate, Long serviceRequestId,
                           String customerName, String customerEmail, String customerPhone, String customerAddress,
                           String vehicleRegistration, String vehicleName, String serviceName, String mechanicName,
                           BigDecimal subtotal, BigDecimal labourCharge, BigDecimal partsCost, BigDecimal tax,
                           BigDecimal total, PaymentStatus paymentStatus, List<ServiceRequestDto.PartLine> parts,
                           PaymentMethod paymentMethod, String transactionId, LocalDateTime paidOn) {}
}
