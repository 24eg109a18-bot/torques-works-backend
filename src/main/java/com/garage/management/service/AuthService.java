package com.garage.management.service;

import com.garage.management.dto.AuthDto;
import com.garage.management.entity.Role;
import com.garage.management.entity.User;
import com.garage.management.exception.DuplicateResourceException;
import com.garage.management.repository.UserRepository;
import com.garage.management.security.CustomUserDetails;
import com.garage.management.security.JwtUtil;
import com.garage.management.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final EntityMapper mapper;

    /** Public registration always creates a CUSTOMER. Mechanics are created by the admin. */
    @Transactional
    public AuthDto.AuthResponse register(AuthDto.RegisterRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
        User user = new User();
        user.setName(req.name().trim());
        user.setEmail(req.email().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setPhone(req.phone());
        user.setAddress(req.address());
        user.setRole(Role.CUSTOMER);
        userRepository.save(user);
        return new AuthDto.AuthResponse(jwtUtil.generateToken(new CustomUserDetails(user)), mapper.user(user));
    }

    @Transactional(readOnly = true)
    public AuthDto.AuthResponse login(AuthDto.LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.email().trim(), req.password()));
        CustomUserDetails details = (CustomUserDetails) auth.getPrincipal();
        return new AuthDto.AuthResponse(jwtUtil.generateToken(details), mapper.user(details.getUser()));
    }
}
