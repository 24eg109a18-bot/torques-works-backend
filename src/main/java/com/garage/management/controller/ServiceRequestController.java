package com.garage.management.controller;

import com.garage.management.dto.ServiceRequestDto;
import com.garage.management.service.ServiceRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-requests")
@RequiredArgsConstructor
public class ServiceRequestController {
    private final ServiceRequestService service;

    /** Admin: all, Mechanic: assigned to him, Customer: his own. */
    @GetMapping
    public List<ServiceRequestDto.Response> list() { return service.list(); }

    @GetMapping("/{id}")
    public ServiceRequestDto.Response get(@PathVariable Long id) { return service.get(id); }

    @GetMapping("/vehicle/{vehicleId}/history")
    public List<ServiceRequestDto.Response> history(@PathVariable Long vehicleId) { return service.vehicleHistory(vehicleId); }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceRequestDto.Response assign(@PathVariable Long id, @Valid @RequestBody ServiceRequestDto.AssignRequest req) {
        return service.assign(id, req.mechanicId());
    }

    @PutMapping("/{id}/status")
    public ServiceRequestDto.Response status(@PathVariable Long id, @Valid @RequestBody ServiceRequestDto.StatusRequest req) {
        return service.changeStatus(id, req.status());
    }

    @PutMapping("/{id}/work")
    @PreAuthorize("hasAnyRole('ADMIN','MECHANIC')")
    public ServiceRequestDto.Response work(@PathVariable Long id, @Valid @RequestBody ServiceRequestDto.WorkRequest req) {
        return service.updateWork(id, req);
    }

    @PostMapping("/{id}/parts")
    @PreAuthorize("hasAnyRole('ADMIN','MECHANIC')")
    public ServiceRequestDto.Response addPart(@PathVariable Long id, @Valid @RequestBody ServiceRequestDto.AddPartRequest req) {
        return service.addPart(id, req);
    }

    @DeleteMapping("/{id}/parts/{usedPartId}")
    @PreAuthorize("hasAnyRole('ADMIN','MECHANIC')")
    public ServiceRequestDto.Response removePart(@PathVariable Long id, @PathVariable Long usedPartId) {
        return service.removePart(id, usedPartId);
    }
}
