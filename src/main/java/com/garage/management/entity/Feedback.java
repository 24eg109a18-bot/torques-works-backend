package com.garage.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "feedback")
@Getter @Setter @NoArgsConstructor
public class Feedback {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "service_request_id", unique = true)
    private ServiceRequest serviceRequest;

    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "customer_id")
    private User customer;

    @Column(nullable = false) private int rating;

    @Column(length = 1000) private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();
}
