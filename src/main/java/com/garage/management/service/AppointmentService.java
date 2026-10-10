package com.garage.management.service;

import com.garage.management.dto.AppointmentDto;
import com.garage.management.dto.ServiceRequestDto;
import com.garage.management.entity.*;
import com.garage.management.exception.InvalidAppointmentException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.exception.UnauthorizedException;
import com.garage.management.repository.*;
import com.garage.management.security.CustomUserDetails;
import com.garage.management.util.CurrentUser;
import com.garage.management.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {
    private static final LocalTime OPENING = LocalTime.of(9, 0);
    private static final LocalTime CLOSING = LocalTime.of(18, 0);

    private final AppointmentRepository appointmentRepository;
    private final ServiceRequestRepository requestRepository;
    private final VehicleRepository vehicleRepository;
    private final GarageServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final ServiceRequestService requestService;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<AppointmentDto.Response> list() {
        CustomUserDetails me = CurrentUser.get();
        List<Appointment> list = me.getRole() == Role.ADMIN
                ? appointmentRepository.findAllByOrderByIdDesc()
                : appointmentRepository.findByCustomer(me.getId());
        return list.stream().map(mapper::appointment).toList();
    }

    /** Customer books an appointment. A matching ServiceRequest (job card) is created in PENDING state. */
    @Transactional
    public AppointmentDto.Response book(AppointmentDto.BookRequest req) {
        User customer = userRepository.findById(CurrentUser.id()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Vehicle vehicle = vehicleRepository.findById(req.vehicleId()).orElseThrow(() -> new ResourceNotFoundException("Vehicle not found"));
        if (!vehicle.getOwner().getId().equals(customer.getId())) throw new UnauthorizedException("This vehicle does not belong to you");
        GarageService service = serviceRepository.findById(req.serviceId()).orElseThrow(() -> new ResourceNotFoundException("Service not found"));
        if (!service.isActive()) throw new InvalidAppointmentException("This service is currently unavailable");

        validateSlot(req.date(), req.time());
        if (requestRepository.vehicleBusy(vehicle.getId(), req.date(), req.time(), EnumSet.of(ServiceStatus.CANCELLED, ServiceStatus.COMPLETED))) {
            throw new InvalidAppointmentException("This vehicle already has a booking for the selected date and time");
        }

        Appointment a = new Appointment();
        a.setCustomer(customer);
        a.setVehicle(vehicle);
        a.setService(service);
        a.setAppointmentDate(req.date());
        a.setAppointmentTime(req.time());
        a.setProblemDescription(req.problemDescription().trim());
        a.setStatus(ServiceStatus.PENDING);
        a = appointmentRepository.save(a);

        ServiceRequest r = new ServiceRequest();
        r.setAppointment(a);
        r.setVehicle(vehicle);
        r.setService(service);
        r.setStatus(ServiceStatus.PENDING);
        a.setServiceRequest(requestRepository.save(r));
        return mapper.appointment(a);
    }

    /** Status of an appointment is the status of its service request, so the same rules apply. */
    @Transactional
    public AppointmentDto.Response updateStatus(Long id, ServiceStatus status) {
        Appointment a = appointmentRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id " + id));
        ServiceRequestDto.Response updated = requestService.changeStatus(a.getServiceRequest().getId(), status);
        return mapper.appointment(appointmentRepository.findById(id).orElseThrow());
    }

    private void validateSlot(LocalDate date, LocalTime time) {
        if (date.isBefore(LocalDate.now())) throw new InvalidAppointmentException("Appointment date cannot be in the past");
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) throw new InvalidAppointmentException("The garage is closed on Sundays");
        if (time.isBefore(OPENING) || time.isAfter(CLOSING)) throw new InvalidAppointmentException("Garage hours are 09:00 to 18:00");
        if (date.equals(LocalDate.now()) && !time.isAfter(LocalTime.now())) throw new InvalidAppointmentException("Selected time has already passed today");
        if (date.isAfter(LocalDate.now().plusMonths(3))) throw new InvalidAppointmentException("Appointments can be booked up to 3 months ahead");
    }
}
