package com.garage.management.repository;

import com.garage.management.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findAllByOrderByIdDesc();

    @Query("select p from Payment p where p.invoice.serviceRequest.vehicle.owner.id = :uid order by p.id desc")
    List<Payment> findByCustomer(@Param("uid") Long customerId);
}
