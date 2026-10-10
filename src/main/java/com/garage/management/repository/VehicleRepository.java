package com.garage.management.repository;

import com.garage.management.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByRegistrationNumber(String registrationNumber);
    boolean existsByRegistrationNumber(String registrationNumber);

    @Query("select v from Vehicle v where v.owner.id = :ownerId order by v.id desc")
    List<Vehicle> findByOwner(@Param("ownerId") Long ownerId);

    @Query("select count(v) from Vehicle v where v.owner.id = :ownerId")
    long countByOwner(@Param("ownerId") Long ownerId);
}
