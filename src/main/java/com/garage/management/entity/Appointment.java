package com.garage.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** The booking made by the customer. Its status mirrors the linked ServiceRequest. */
@Entity
@Table(name = "appointments")
@Getter @Setter @NoArgsConstructor
public class Appointment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "service_id")
    private GarageService service;

    @Column(nullable = false) private LocalDate appointmentDate;
    @Column(nullable = false) private LocalTime appointmentTime;

    @Column(length = 1000)
    private String problemDescription;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private ServiceStatus status = ServiceStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToOne(mappedBy = "appointment")
    private ServiceRequest serviceRequest;
}
