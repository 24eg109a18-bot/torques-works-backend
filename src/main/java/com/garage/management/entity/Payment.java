package com.garage.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter @Setter @NoArgsConstructor
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", unique = true)
    private Invoice invoice;

    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Column(nullable = false) private LocalDateTime paymentDate;
    @Column(nullable = false, length = 50) private String transactionId;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PAID;
}
