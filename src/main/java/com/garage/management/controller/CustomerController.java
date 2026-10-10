package com.garage.management.controller;

import com.garage.management.dto.UserDto;
import com.garage.management.entity.Role;
import com.garage.management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserDto.Response> all() { return userService.listByRole(Role.CUSTOMER); }

    /** Admin can read anyone; a customer can read only himself. */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public UserDto.Response get(@PathVariable Long id) { return userService.get(id, Role.CUSTOMER); }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == principal.id")
    public UserDto.Response update(@PathVariable Long id, @Valid @RequestBody UserDto.UpdateRequest req) {
        return userService.update(id, Role.CUSTOMER, req);
    }

    @PutMapping("/{id}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public UserDto.Response toggleBlock(@PathVariable Long id) { return userService.toggleBlock(id, Role.CUSTOMER); }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id, Role.CUSTOMER);
        return ResponseEntity.noContent().build();
    }
}
