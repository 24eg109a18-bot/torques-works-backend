package com.garage.management.repository;

import com.garage.management.entity.Role;
import com.garage.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
    List<User> findByRoleOrderByIdDesc(Role role);
    long countByRole(Role role);
}
