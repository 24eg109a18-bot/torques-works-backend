package com.garage.management.controller;

import com.garage.management.dto.PaymentDto;
import com.garage.management.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public List<PaymentDto.Response> list() { return paymentService.list(); }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public ResponseEntity<PaymentDto.Response> pay(@Valid @RequestBody PaymentDto.PayRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.pay(req));
    }
}
