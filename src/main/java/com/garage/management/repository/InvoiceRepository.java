package com.garage.management.repository;

import com.garage.management.entity.Invoice;
import com.garage.management.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    List<Invoice> findAllByOrderByIdDesc();
    List<Invoice> findByPaymentStatus(PaymentStatus status);

    @Query("select i from Invoice i where i.serviceRequest.vehicle.owner.id = :uid order by i.id desc")
    List<Invoice> findByCustomer(@Param("uid") Long customerId);
}
