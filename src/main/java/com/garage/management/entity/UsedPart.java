package com.garage.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "used_parts")
@Getter @Setter @NoArgsConstructor
public class UsedPart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "service_request_id")
    private ServiceRequest serviceRequest;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "spare_part_id")
    private SparePart sparePart;

    @Column(nullable = false) private int quantity;

    /** Price captured at the time of use so later price changes do not alter old invoices. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;
}
