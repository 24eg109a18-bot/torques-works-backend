package com.garage.management.controller;

import com.garage.management.dto.AppointmentDto;
import com.garage.management.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<AppointmentDto.Response> book(@Valid @RequestBody AppointmentDto.BookRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentService.book(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public List<AppointmentDto.Response> list() { return appointmentService.list(); }

    /** Admin: approve/cancel. Customer: cancel own. Transition rules are checked in the service layer. */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public AppointmentDto.Response updateStatus(@PathVariable Long id, @Valid @RequestBody AppointmentDto.StatusRequest req) {
        return appointmentService.updateStatus(id, req.status());
    }
}
