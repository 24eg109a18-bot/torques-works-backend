package com.garage.management.util;

import com.garage.management.dto.*;
import com.garage.management.entity.*;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** Converts JPA entities to response DTOs so entities are never exposed through the API. */
@Component
public class EntityMapper {

    public UserDto.Response user(User u) {
        return new UserDto.Response(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getAddress(),
                u.getRole(), u.getSpecialization(), u.isActive(), u.getCreatedAt());
    }

    public VehicleDto.Response vehicle(Vehicle v) {
        return new VehicleDto.Response(v.getId(), v.getRegistrationNumber(), v.getBrand(), v.getModel(), v.getVariant(),
                v.getManufacturingYear(), v.getFuelType(), v.getCurrentKm(), v.getColor(), v.getInsuranceExpiry(),
                v.getRcNumber(), v.getOwner().getId(), v.getOwner().getName());
    }

    public ServiceDto.Response service(GarageService s) {
        return new ServiceDto.Response(s.getId(), s.getName(), s.getDescription(), s.getEstimatedCost(),
                s.getEstimatedDurationHours(), s.isActive());
    }

    public PartDto.Response part(SparePart p) {
        return new PartDto.Response(p.getId(), p.getName(), p.getPartNumber(), p.getBrand(), p.getCategory(),
                p.getPrice(), p.getQuantity(), p.getMinStockLevel(), p.isLowStock());
    }

    public ServiceRequestDto.PartLine line(UsedPart u) {
        BigDecimal total = u.getUnitPrice().multiply(BigDecimal.valueOf(u.getQuantity()));
        return new ServiceRequestDto.PartLine(u.getId(), u.getSparePart().getId(), u.getSparePart().getName(),
                u.getSparePart().getPartNumber(), u.getQuantity(), u.getUnitPrice(), total);
    }

    public AppointmentDto.Response appointment(Appointment a) {
        ServiceRequest r = a.getServiceRequest();
        Vehicle v = a.getVehicle();
        return new AppointmentDto.Response(a.getId(), a.getCustomer().getId(), a.getCustomer().getName(), v.getId(),
                v.getRegistrationNumber(), v.getBrand() + " " + v.getModel(), a.getService().getId(),
                a.getService().getName(), a.getAppointmentDate(), a.getAppointmentTime(), a.getProblemDescription(),
                a.getStatus(), a.getCreatedAt(), r == null ? null : r.getId(),
                r == null || r.getMechanic() == null ? null : r.getMechanic().getName());
    }

    public ServiceRequestDto.Response request(ServiceRequest r) {
        Appointment a = r.getAppointment();
        Vehicle v = r.getVehicle();
        User c = v.getOwner();
        User m = r.getMechanic();
        Invoice inv = r.getInvoice();
        List<ServiceRequestDto.PartLine> parts = r.getUsedParts().stream().map(this::line).toList();
        BigDecimal partsTotal = parts.stream().map(ServiceRequestDto.PartLine::lineTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ServiceRequestDto.Response(r.getId(), a.getId(), c.getId(), c.getName(), c.getPhone(),
                v.getId(), v.getRegistrationNumber(), v.getBrand() + " " + v.getModel(),
                r.getService().getId(), r.getService().getName(), r.getService().getEstimatedCost(),
                m == null ? null : m.getId(), m == null ? null : m.getName(), r.getStatus(),
                a.getAppointmentDate(), a.getAppointmentTime(), a.getProblemDescription(),
                r.getInspectionNotes(), r.getLabourCharge(), r.getEstimatedCompletionDate(),
                r.getCreatedAt(), r.getCompletedAt(), parts, partsTotal,
                inv == null ? null : inv.getId(), inv == null ? null : inv.getPaymentStatus(),
                r.getFeedback() != null, List.copyOf(r.getStatus().next()));
    }

    public InvoiceDto.Response invoice(Invoice i) {
        ServiceRequest r = i.getServiceRequest();
        Vehicle v = r.getVehicle();
        User c = v.getOwner();
        Payment p = i.getPayment();
        return new InvoiceDto.Response(i.getId(), i.getInvoiceNumber(), i.getInvoiceDate(), r.getId(),
                c.getName(), c.getEmail(), c.getPhone(), c.getAddress(),
                v.getRegistrationNumber(), v.getBrand() + " " + v.getModel(), r.getService().getName(),
                r.getMechanic() == null ? null : r.getMechanic().getName(),
                i.getSubtotal(), i.getLabourCharge(), i.getPartsCost(), i.getTax(), i.getTotal(), i.getPaymentStatus(),
                r.getUsedParts().stream().map(this::line).toList(),
                p == null ? null : p.getMethod(), p == null ? null : p.getTransactionId(),
                p == null ? null : p.getPaymentDate());
    }

    public PaymentDto.Response payment(Payment p) {
        Invoice i = p.getInvoice();
        return new PaymentDto.Response(p.getId(), i.getId(), i.getInvoiceNumber(),
                i.getServiceRequest().getVehicle().getOwner().getName(), p.getAmount(), p.getMethod(),
                p.getPaymentDate(), p.getTransactionId(), p.getStatus());
    }

    public FeedbackDto.Response feedback(Feedback f) {
        ServiceRequest r = f.getServiceRequest();
        return new FeedbackDto.Response(f.getId(), r.getId(), f.getCustomer().getName(), r.getService().getName(),
                r.getVehicle().getRegistrationNumber(), f.getRating(), f.getComment(), f.getCreatedAt());
    }
}
