package com.garage.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** A service offered by the garage (General Service, Oil Change ...). */
@Entity
@Table(name = "garage_services")
@Getter @Setter @NoArgsConstructor
public class GarageService {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal estimatedCost;

    @Column(nullable = false)
    private Integer estimatedDurationHours;

    @Column(nullable = false)
    private boolean active = true;
}
