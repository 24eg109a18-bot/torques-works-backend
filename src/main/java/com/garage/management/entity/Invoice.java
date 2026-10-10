package com.garage.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
@Getter @Setter @NoArgsConstructor
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String invoiceNumber;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id", unique = true)
    private ServiceRequest serviceRequest;

    @Column(nullable = false) private LocalDate invoiceDate;

    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal subtotal;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal labourCharge;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal partsCost;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal tax;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal total;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @OneToOne(mappedBy = "invoice")
    private Payment payment;
}
