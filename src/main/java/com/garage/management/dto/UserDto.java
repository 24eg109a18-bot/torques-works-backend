package com.garage.management.dto;

import com.garage.management.entity.Role;
import com.garage.management.util.Patterns;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class UserDto {
    public record Response(Long id, String name, String email, String phone, String address, Role role,
                           String specialization, boolean active, LocalDateTime createdAt) {}

    /** Used for customer profile updates and admin edits of customers/mechanics. */
    public record UpdateRequest(
            @NotBlank(message = "Name is required") @Size(min = 2, max = 100, message = "Name must be 2-100 characters") String name,
            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = Patterns.PHONE, message = "Enter a valid 10-digit mobile number") String phone,
            @Size(max = 255, message = "Address is too long") String address,
            String specialization) {}

    public record MechanicRequest(
            @NotBlank(message = "Name is required") @Size(min = 2, max = 100) String name,
            @NotBlank(message = "Email is required") @Email(message = "Enter a valid email address") String email,
            @NotBlank(message = "Password is required")
            @Pattern(regexp = Patterns.PASSWORD, message = "Password needs 8+ characters with upper, lower, digit and special character") String password,
            @NotBlank(message = "Phone number is required")
            @Pattern(regexp = Patterns.PHONE, message = "Enter a valid 10-digit mobile number") String phone,
            String specialization) {}
}
