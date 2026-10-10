package com.garage.management.repository;

import com.garage.management.entity.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    List<Feedback> findAllByOrderByIdDesc();
    List<Feedback> findByCustomerIdOrderByIdDesc(Long customerId);
    boolean existsByServiceRequestId(Long serviceRequestId);
}
