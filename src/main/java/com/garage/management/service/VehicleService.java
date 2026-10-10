package com.garage.management.service;

import com.garage.management.dto.VehicleDto;
import com.garage.management.entity.Role;
import com.garage.management.entity.User;
import com.garage.management.entity.Vehicle;
import com.garage.management.exception.BadRequestException;
import com.garage.management.exception.DuplicateResourceException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.exception.UnauthorizedException;
import com.garage.management.repository.ServiceRequestRepository;
import com.garage.management.repository.UserRepository;
import com.garage.management.repository.VehicleRepository;
import com.garage.management.security.CustomUserDetails;
import com.garage.management.util.CurrentUser;
import com.garage.management.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Year;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {
    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final ServiceRequestRepository requestRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<VehicleDto.Response> list() {
        CustomUserDetails me = CurrentUser.get();
        List<Vehicle> vehicles = me.getRole() == Role.ADMIN ? vehicleRepository.findAll() : vehicleRepository.findByOwner(me.getId());
        return vehicles.stream().map(mapper::vehicle).toList();
    }

    @Transactional(readOnly = true)
    public VehicleDto.Response get(Long id) {
        Vehicle v = find(id);
        checkAccess(v);
        return mapper.vehicle(v);
    }

    @Transactional
    public VehicleDto.Response create(VehicleDto.Request req) {
        String reg = normalise(req.registrationNumber());
        if (vehicleRepository.existsByRegistrationNumber(reg)) {
            throw new DuplicateResourceException("A vehicle with registration number " + reg + " already exists");
        }
        validateYear(req.manufacturingYear());
        User owner = userRepository.findById(CurrentUser.id()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Vehicle v = new Vehicle();
        v.setOwner(owner);
        apply(v, req, reg);
        return mapper.vehicle(vehicleRepository.save(v));
    }

    @Transactional
    public VehicleDto.Response update(Long id, VehicleDto.Request req) {
        Vehicle v = find(id);
        checkAccess(v);
        String reg = normalise(req.registrationNumber());
        if (!reg.equals(v.getRegistrationNumber()) && vehicleRepository.existsByRegistrationNumber(reg)) {
            throw new DuplicateResourceException("A vehicle with registration number " + reg + " already exists");
        }
        validateYear(req.manufacturingYear());
        apply(v, req, reg);
        return mapper.vehicle(v);
    }

    @Transactional
    public void delete(Long id) {
        Vehicle v = find(id);
        checkAccess(v);
        if (requestRepository.existsForVehicle(id)) {
            throw new BadRequestException("This vehicle has service history and cannot be deleted");
        }
        vehicleRepository.delete(v);
    }

    private void apply(Vehicle v, VehicleDto.Request r, String reg) {
        v.setRegistrationNumber(reg);
        v.setBrand(r.brand().trim());
        v.setModel(r.model().trim());
        v.setVariant(r.variant());
        v.setManufacturingYear(r.manufacturingYear());
        v.setFuelType(r.fuelType());
        v.setCurrentKm(r.currentKm());
        v.setColor(r.color());
        v.setInsuranceExpiry(r.insuranceExpiry());
        v.setRcNumber(r.rcNumber());
    }

    private void validateYear(Integer year) {
        if (year > Year.now().getValue() + 1) throw new BadRequestException("Manufacturing year cannot be in the future");
    }

    private String normalise(String reg) { return reg.replaceAll("[ -]", "").toUpperCase(); }

    private Vehicle find(Long id) {
        return vehicleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id " + id));
    }

    /** A customer may only touch his own vehicles; admin may touch all. */
    private void checkAccess(Vehicle v) {
        CustomUserDetails me = CurrentUser.get();
        if (me.getRole() != Role.ADMIN && !v.getOwner().getId().equals(me.getId())) {
            throw new UnauthorizedException("This vehicle does not belong to you");
        }
    }
}
