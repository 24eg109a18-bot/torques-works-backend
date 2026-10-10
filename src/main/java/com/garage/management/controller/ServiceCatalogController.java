package com.garage.management.controller;

import com.garage.management.dto.ServiceDto;
import com.garage.management.service.ServiceCatalogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class ServiceCatalogController {
    private final ServiceCatalogService catalogService;

    /** Public: used by the website's Services page (no login needed). */
    @GetMapping("/public")
    public List<ServiceDto.Response> publicList() { return catalogService.listActive(); }

    /** Admin sees all services (also inactive), others see active ones. */
    @GetMapping
    public List<ServiceDto.Response> list() { return catalogService.list(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServiceDto.Response> create(@Valid @RequestBody ServiceDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServiceDto.Response update(@PathVariable Long id, @Valid @RequestBody ServiceDto.Request req) {
        return catalogService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        catalogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
