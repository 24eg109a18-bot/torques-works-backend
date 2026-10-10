package com.garage.management.service;

import com.garage.management.dto.ServiceRequestDto;
import com.garage.management.entity.*;
import com.garage.management.exception.*;
import com.garage.management.repository.ServiceRequestRepository;
import com.garage.management.repository.SparePartRepository;
import com.garage.management.repository.UserRepository;
import com.garage.management.security.CustomUserDetails;
import com.garage.management.util.CurrentUser;
import com.garage.management.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.garage.management.entity.ServiceStatus.*;

/** Core of the workflow: assignment, status changes, inspection notes, labour and spare parts. */
@Service
@RequiredArgsConstructor
public class ServiceRequestService {
    /** Statuses in which a mechanic is occupied with the job (used for double-booking checks and editing). */
    public static final Set<ServiceStatus> OCCUPIED = EnumSet.of(ASSIGNED, IN_PROGRESS, WAITING_FOR_PARTS);

    private final ServiceRequestRepository repository;
    private final UserRepository userRepository;
    private final SparePartRepository partRepository;
    private final InvoiceService invoiceService;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<ServiceRequestDto.Response> list() {
        CustomUserDetails me = CurrentUser.get();
        List<ServiceRequest> list = switch (me.getRole()) {
            case ADMIN -> repository.findAllByOrderByIdDesc();
            case MECHANIC -> repository.findByMechanic(me.getId());
            case CUSTOMER -> repository.findByCustomer(me.getId());
        };
        return list.stream().map(mapper::request).toList();
    }

    @Transactional(readOnly = true)
    public ServiceRequestDto.Response get(Long id) {
        ServiceRequest r = load(id);
        checkAccess(r);
        return mapper.request(r);
    }

    /** Completed jobs of one vehicle (used by mechanics to see previous service history). */
    @Transactional(readOnly = true)
    public List<ServiceRequestDto.Response> vehicleHistory(Long vehicleId) {
        CustomUserDetails me = CurrentUser.get();
        List<ServiceRequest> list = repository.findByVehicleAndStatus(vehicleId, COMPLETED);
        if (me.getRole() == Role.CUSTOMER && list.stream().anyMatch(r -> !r.getVehicle().getOwner().getId().equals(me.getId()))) {
            throw new UnauthorizedException("This vehicle does not belong to you");
        }
        return list.stream().map(mapper::request).toList();
    }

    @Transactional
    public ServiceRequestDto.Response assign(Long id, Long mechanicId) {
        ServiceRequest r = load(id);
        if (!EnumSet.of(PENDING, APPROVED, ASSIGNED).contains(r.getStatus())) {
            throw new InvalidAppointmentException("A mechanic can only be assigned to pending, approved or assigned requests");
        }
        User mechanic = userRepository.findById(mechanicId)
                .filter(u -> u.getRole() == Role.MECHANIC)
                .orElseThrow(() -> new ResourceNotFoundException("Mechanic not found with id " + mechanicId));
        if (!mechanic.isActive()) throw new InvalidAppointmentException("This mechanic's account is blocked");

        Appointment a = r.getAppointment();
        if (repository.mechanicBusy(mechanicId, r.getId(), a.getAppointmentDate(), a.getAppointmentTime(), OCCUPIED)) {
            throw new InvalidAppointmentException(mechanic.getName() + " is already booked on " + a.getAppointmentDate()
                    + " at " + a.getAppointmentTime() + ". Choose another mechanic.");
        }
        r.setMechanic(mechanic);
        r.setStatus(ASSIGNED);
        a.setStatus(ASSIGNED);
        return mapper.request(r);
    }

    @Transactional
    public ServiceRequestDto.Response changeStatus(Long id, ServiceStatus target) {
        ServiceRequest r = load(id);
        CustomUserDetails me = CurrentUser.get();
        checkAccess(r);

        switch (me.getRole()) {
            case CUSTOMER -> {
                if (target != CANCELLED) throw new UnauthorizedException("Customers can only cancel their appointments");
                if (r.getStatus() != PENDING && r.getStatus() != APPROVED) {
                    throw new InvalidAppointmentException("Only pending or approved appointments can be cancelled");
                }
            }
            case MECHANIC -> {
                if (!EnumSet.of(IN_PROGRESS, WAITING_FOR_PARTS, COMPLETED).contains(target)) {
                    throw new UnauthorizedException("Mechanics can only set In Progress, Waiting For Parts or Completed");
                }
            }
            case ADMIN -> {
                if (target == ASSIGNED) throw new InvalidAppointmentException("Use the assign option to assign a mechanic");
            }
        }
        if (!r.getStatus().canMoveTo(target)) {
            throw new InvalidAppointmentException("Invalid status change: " + r.getStatus() + " -> " + target);
        }

        if (target == CANCELLED) restoreStock(r);
        r.setStatus(target);
        r.getAppointment().setStatus(target);
        if (target == COMPLETED) {
            r.setCompletedAt(LocalDateTime.now());
            invoiceService.generateFor(r);       // invoice is created automatically
        }
        return mapper.request(r);
    }

    @Transactional
    public ServiceRequestDto.Response updateWork(Long id, ServiceRequestDto.WorkRequest req) {
        ServiceRequest r = load(id);
        checkAccess(r);
        ensureEditable(r);
        if (req.inspectionNotes() != null) r.setInspectionNotes(req.inspectionNotes());
        if (req.labourCharge() != null) r.setLabourCharge(req.labourCharge());
        if (req.estimatedCompletionDate() != null) {
            if (req.estimatedCompletionDate().isBefore(r.getAppointment().getAppointmentDate())) {
                throw new BadRequestException("Estimated completion cannot be before the appointment date");
            }
            r.setEstimatedCompletionDate(req.estimatedCompletionDate());
        }
        return mapper.request(r);
    }

    /** Adds a part to the job and automatically decreases inventory. */
    @Transactional
    public ServiceRequestDto.Response addPart(Long id, ServiceRequestDto.AddPartRequest req) {
        ServiceRequest r = load(id);
        checkAccess(r);
        ensureEditable(r);
        SparePart part = partRepository.findById(req.partId())
                .orElseThrow(() -> new ResourceNotFoundException("Spare part not found"));
        if (part.getQuantity() < req.quantity()) {
            throw new BadRequestException("Insufficient stock for " + part.getName() + ". Available: " + part.getQuantity());
        }
        part.setQuantity(part.getQuantity() - req.quantity());

        Optional<UsedPart> existing = r.getUsedParts().stream()
                .filter(u -> u.getSparePart().getId().equals(part.getId())).findFirst();
        if (existing.isPresent()) {
            existing.get().setQuantity(existing.get().getQuantity() + req.quantity());
        } else {
            UsedPart u = new UsedPart();
            u.setServiceRequest(r);
            u.setSparePart(part);
            u.setQuantity(req.quantity());
            u.setUnitPrice(part.getPrice());
            r.getUsedParts().add(u);
        }
        return mapper.request(repository.saveAndFlush(r));
    }

    @Transactional
    public ServiceRequestDto.Response removePart(Long id, Long usedPartId) {
        ServiceRequest r = load(id);
        checkAccess(r);
        ensureEditable(r);
        UsedPart u = r.getUsedParts().stream().filter(x -> x.getId().equals(usedPartId)).findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Part line not found on this job"));
        u.getSparePart().setQuantity(u.getSparePart().getQuantity() + u.getQuantity());   // give stock back
        r.getUsedParts().remove(u);
        return mapper.request(repository.saveAndFlush(r));
    }

    // ---------------- helpers ----------------
    private void restoreStock(ServiceRequest r) {
        r.getUsedParts().forEach(u -> u.getSparePart().setQuantity(u.getSparePart().getQuantity() + u.getQuantity()));
    }

    private void ensureEditable(ServiceRequest r) {
        if (!OCCUPIED.contains(r.getStatus())) {
            throw new InvalidAppointmentException("Work can only be updated while the job is assigned, in progress or waiting for parts");
        }
    }

    private ServiceRequest load(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service request not found with id " + id));
    }

    private void checkAccess(ServiceRequest r) {
        CustomUserDetails me = CurrentUser.get();
        boolean allowed = switch (me.getRole()) {
            case ADMIN -> true;
            case MECHANIC -> r.getMechanic() != null && r.getMechanic().getId().equals(me.getId());
            case CUSTOMER -> r.getVehicle().getOwner().getId().equals(me.getId());
        };
        if (!allowed) throw new UnauthorizedException("You do not have access to this service request");
    }
}
