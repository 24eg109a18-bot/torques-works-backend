package com.garage.management.service;

import com.garage.management.dto.UserDto;
import com.garage.management.entity.Role;
import com.garage.management.entity.User;
import com.garage.management.exception.BadRequestException;
import com.garage.management.exception.DuplicateResourceException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.repository.UserRepository;
import com.garage.management.util.CurrentUser;
import com.garage.management.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Customer and mechanic management (profile updates, blocking, deleting). */
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<UserDto.Response> listByRole(Role role) {
        return userRepository.findByRoleOrderByIdDesc(role).stream().map(mapper::user).toList();
    }

    @Transactional(readOnly = true)
    public UserDto.Response get(Long id, Role expectedRole) {
        return mapper.user(find(id, expectedRole));
    }

    @Transactional(readOnly = true)
    public UserDto.Response me() {
        return mapper.user(userRepository.findById(CurrentUser.id())
                .orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @Transactional
    public UserDto.Response update(Long id, Role expectedRole, UserDto.UpdateRequest req) {
        User u = find(id, expectedRole);
        u.setName(req.name().trim());
        u.setPhone(req.phone());
        u.setAddress(req.address());
        if (u.getRole() == Role.MECHANIC && req.specialization() != null) u.setSpecialization(req.specialization());
        return mapper.user(u);
    }

    @Transactional
    public UserDto.Response createMechanic(UserDto.MechanicRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
        User u = new User();
        u.setName(req.name().trim());
        u.setEmail(req.email().trim().toLowerCase());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setPhone(req.phone());
        u.setSpecialization(req.specialization());
        u.setRole(Role.MECHANIC);
        return mapper.user(userRepository.save(u));
    }

    @Transactional
    public UserDto.Response toggleBlock(Long id, Role expectedRole) {
        User u = find(id, expectedRole);
        u.setActive(!u.isActive());
        return mapper.user(u);
    }

    @Transactional
    public void delete(Long id, Role expectedRole) {
        User u = find(id, expectedRole);
        try {
            userRepository.delete(u);
            userRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("This user has appointments or service history and cannot be deleted. Block the account instead.");
        }
    }

    private User find(Long id, Role expectedRole) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        if (u.getRole() != expectedRole) throw new ResourceNotFoundException("User not found with id " + id);
        return u;
    }
}
