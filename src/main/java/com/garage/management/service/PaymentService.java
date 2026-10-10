package com.garage.management.service;

import com.garage.management.dto.PaymentDto;
import com.garage.management.entity.*;
import com.garage.management.exception.BadRequestException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.exception.UnauthorizedException;
import com.garage.management.repository.InvoiceRepository;
import com.garage.management.repository.PaymentRepository;
import com.garage.management.security.CustomUserDetails;
import com.garage.management.util.CurrentUser;
import com.garage.management.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Simulated payments (no real gateway): a transaction id is generated and the invoice is marked PAID. */
@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<PaymentDto.Response> list() {
        CustomUserDetails me = CurrentUser.get();
        List<Payment> list = me.getRole() == Role.ADMIN ? paymentRepository.findAllByOrderByIdDesc() : paymentRepository.findByCustomer(me.getId());
        return list.stream().map(mapper::payment).toList();
    }

    @Transactional
    public PaymentDto.Response pay(PaymentDto.PayRequest req) {
        Invoice invoice = invoiceRepository.findById(req.invoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id " + req.invoiceId()));
        CustomUserDetails me = CurrentUser.get();
        if (me.getRole() != Role.ADMIN && !invoice.getServiceRequest().getVehicle().getOwner().getId().equals(me.getId())) {
            throw new UnauthorizedException("This invoice does not belong to you");
        }
        if (invoice.getPaymentStatus() == PaymentStatus.PAID) throw new BadRequestException("This invoice is already paid");

        Payment p = new Payment();
        p.setInvoice(invoice);
        p.setAmount(invoice.getTotal());
        p.setMethod(req.method());
        p.setPaymentDate(LocalDateTime.now());
        p.setTransactionId((req.method() == PaymentMethod.CASH ? "CASH-" : "TXN-") + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        p.setStatus(PaymentStatus.PAID);
        invoice.setPaymentStatus(PaymentStatus.PAID);
        Payment saved = paymentRepository.save(p);
        invoice.setPayment(saved);
        return mapper.payment(saved);
    }
}
