package com.garage.management.repository;

import com.garage.management.entity.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SparePartRepository extends JpaRepository<SparePart, Long> {
    boolean existsByPartNumberIgnoreCase(String partNumber);

    @Query("select p from SparePart p where p.quantity < p.minStockLevel order by p.quantity asc")
    List<SparePart> findLowStock();

    @Query("select count(p) from SparePart p where p.quantity < p.minStockLevel")
    long countLowStock();
}
