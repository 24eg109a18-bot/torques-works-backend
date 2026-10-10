package com.garage.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** The actual job card: mechanic, status, inspection notes, labour and parts used. */
@Entity
@Table(name = "service_requests")
@Getter @Setter @NoArgsConstructor
public class ServiceRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "service_id")
    private GarageService service;

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "mechanic_id")
    private User mechanic;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private ServiceStatus status = ServiceStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String inspectionNotes;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal labourCharge = BigDecimal.ZERO;

    private LocalDate estimatedCompletionDate;
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime completedAt;

    /** Many-to-many between ServiceRequest and SparePart, resolved through UsedPart (it carries quantity & price). */
    @OneToMany(mappedBy = "serviceRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<UsedPart> usedParts = new ArrayList<>();

    @OneToOne(mappedBy = "serviceRequest")
    private Invoice invoice;

    @OneToOne(mappedBy = "serviceRequest")
    private Feedback feedback;
}
