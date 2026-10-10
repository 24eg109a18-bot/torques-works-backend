package com.garage.management.repository;

import com.garage.management.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findAllByOrderByIdDesc();

    @Query("select a from Appointment a where a.customer.id = :cid order by a.id desc")
    List<Appointment> findByCustomer(@Param("cid") Long customerId);
}
