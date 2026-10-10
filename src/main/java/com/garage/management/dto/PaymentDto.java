package com.garage.management.dto;

import com.garage.management.entity.PaymentMethod;
import com.garage.management.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentDto {
    public record PayRequest(
            @NotNull(message = "Invoice is required") Long invoiceId,
            @NotNull(message = "Payment method is required") PaymentMethod method) {}

    public record Response(Long id, Long invoiceId, String invoiceNumber, String customerName, BigDecimal amount,
                           PaymentMethod method, LocalDateTime paymentDate, String transactionId, PaymentStatus status) {}
}
