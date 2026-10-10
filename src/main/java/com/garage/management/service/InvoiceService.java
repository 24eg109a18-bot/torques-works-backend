package com.garage.management.service;

import com.garage.management.dto.InvoiceDto;
import com.garage.management.entity.*;
import com.garage.management.exception.BadRequestException;
import com.garage.management.exception.DuplicateResourceException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.exception.UnauthorizedException;
import com.garage.management.repository.InvoiceRepository;
import com.garage.management.repository.ServiceRequestRepository;
import com.garage.management.security.CustomUserDetails;
import com.garage.management.util.CurrentUser;
import com.garage.management.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/** Total = Subtotal (service cost) + Labour + Spare parts + Tax. */
@Service
@RequiredArgsConstructor
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final ServiceRequestRepository requestRepository;
    private final EntityMapper mapper;

    @Value("${app.tax-rate}")
    private BigDecimal taxRate;

    /** Called automatically when a job is marked COMPLETED. */
    @Transactional
    public Invoice generateFor(ServiceRequest r) {
        if (r.getInvoice() != null) return r.getInvoice();

        BigDecimal subtotal = r.getService().getEstimatedCost();
        BigDecimal labour = r.getLabourCharge() == null ? BigDecimal.ZERO : r.getLabourCharge();
        BigDecimal parts = r.getUsedParts().stream()
                .map(u -> u.getUnitPrice().multiply(BigDecimal.valueOf(u.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal taxable = subtotal.add(labour).add(parts);
        BigDecimal tax = taxable.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);

        Invoice i = new Invoice();
        i.setServiceRequest(r);
        i.setInvoiceDate(LocalDate.now());
        i.setSubtotal(subtotal);
        i.setLabourCharge(labour);
        i.setPartsCost(parts);
        i.setTax(tax);
        i.setTotal(taxable.add(tax));
        i.setPaymentStatus(PaymentStatus.PENDING);
        i.setInvoiceNumber("TMP-" + System.nanoTime());
        i = invoiceRepository.save(i);
        i.setInvoiceNumber(String.format("INV-%d-%05d", LocalDate.now().getYear(), i.getId()));
        r.setInvoice(i);
        return i;
    }

    @Transactional(readOnly = true)
    public List<InvoiceDto.Response> list() {
        CustomUserDetails me = CurrentUser.get();
        List<Invoice> list = me.getRole() == Role.ADMIN ? invoiceRepository.findAllByOrderByIdDesc() : invoiceRepository.findByCustomer(me.getId());
        return list.stream().map(mapper::invoice).toList();
    }

    @Transactional(readOnly = true)
    public InvoiceDto.Response get(Long id) {
        Invoice i = invoiceRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id " + id));
        CustomUserDetails me = CurrentUser.get();
        if (me.getRole() != Role.ADMIN && !i.getServiceRequest().getVehicle().getOwner().getId().equals(me.getId())) {
            throw new UnauthorizedException("This invoice does not belong to you");
        }
        return mapper.invoice(i);
    }

    /** Manual generation by admin (normally the invoice already exists after completion). */
    @Transactional
    public InvoiceDto.Response create(InvoiceDto.CreateRequest req) {
        ServiceRequest r = requestRepository.findById(req.serviceRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));
        if (r.getStatus() != ServiceStatus.COMPLETED) throw new BadRequestException("Invoice can only be generated for completed services");
        if (r.getInvoice() != null) throw new DuplicateResourceException("Invoice already exists for this service request");
        return mapper.invoice(generateFor(r));
    }
}
