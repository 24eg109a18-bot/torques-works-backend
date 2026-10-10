package com.garage.management.repository;

import com.garage.management.entity.GarageService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GarageServiceRepository extends JpaRepository<GarageService, Long> {
    boolean existsByNameIgnoreCase(String name);
    List<GarageService> findByActiveTrueOrderByNameAsc();
}
