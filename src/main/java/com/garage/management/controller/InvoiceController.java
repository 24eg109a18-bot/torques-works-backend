package com.garage.management.controller;

import com.garage.management.dto.InvoiceDto;
import com.garage.management.service.InvoiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public List<InvoiceDto.Response> list() { return invoiceService.list(); }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public InvoiceDto.Response get(@PathVariable Long id) { return invoiceService.get(id); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<InvoiceDto.Response> create(@Valid @RequestBody InvoiceDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(req));
    }
}
