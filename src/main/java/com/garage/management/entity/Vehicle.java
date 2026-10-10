package com.garage.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "vehicles")
@Getter @Setter @NoArgsConstructor
public class Vehicle {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String registrationNumber;

    @Column(nullable = false) private String brand;
    @Column(nullable = false) private String model;
    private String variant;
    private Integer manufacturingYear;
    private String fuelType;
    private Integer currentKm;
    private String color;
    private LocalDate insuranceExpiry;
    private String rcNumber;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private User owner;
}
