package com.garage.management.controller;

import com.garage.management.dto.PartDto;
import com.garage.management.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spare-parts")
@RequiredArgsConstructor
public class SparePartController {
    private final InventoryService inventoryService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MECHANIC')")
    public List<PartDto.Response> list() { return inventoryService.list(); }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public List<PartDto.Response> lowStock() { return inventoryService.lowStock(); }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PartDto.Response> create(@Valid @RequestBody PartDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.create(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public PartDto.Response update(@PathVariable Long id, @Valid @RequestBody PartDto.Request req) {
        return inventoryService.update(id, req);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        inventoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
