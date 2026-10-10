package com.garage.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "spare_parts")
@Getter @Setter @NoArgsConstructor
public class SparePart {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String name;
    @Column(nullable = false, unique = true, length = 50) private String partNumber;
    private String brand;
    private String category;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false) private int quantity;
    @Column(nullable = false) private int minStockLevel;

    public boolean isLowStock() { return quantity < minStockLevel; }
}
