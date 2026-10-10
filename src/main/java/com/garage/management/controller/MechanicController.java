package com.garage.management.controller;

import com.garage.management.dto.UserDto;
import com.garage.management.entity.Role;
import com.garage.management.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mechanics")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class MechanicController {
    private final UserService userService;

    @GetMapping
    public List<UserDto.Response> all() { return userService.listByRole(Role.MECHANIC); }

    @GetMapping("/{id}")
    public UserDto.Response get(@PathVariable Long id) { return userService.get(id, Role.MECHANIC); }

    @PostMapping
    public ResponseEntity<UserDto.Response> create(@Valid @RequestBody UserDto.MechanicRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createMechanic(req));
    }

    @PutMapping("/{id}")
    public UserDto.Response update(@PathVariable Long id, @Valid @RequestBody UserDto.UpdateRequest req) {
        return userService.update(id, Role.MECHANIC, req);
    }

    @PutMapping("/{id}/block")
    public UserDto.Response toggleBlock(@PathVariable Long id) { return userService.toggleBlock(id, Role.MECHANIC); }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id, Role.MECHANIC);
        return ResponseEntity.noContent().build();
    }
}
