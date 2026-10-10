package com.garage.management.service;

import com.garage.management.dto.DashboardDto;
import com.garage.management.dto.DashboardDto.*;
import com.garage.management.entity.*;
import com.garage.management.repository.*;
import com.garage.management.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Statistics for the three dashboards and the admin reports page. */
@Service
@RequiredArgsConstructor
public class DashboardService {
    private static final Set<ServiceStatus> WORKING = EnumSet.of(ServiceStatus.IN_PROGRESS, ServiceStatus.WAITING_FOR_PARTS);
    private static final Set<ServiceStatus> UPCOMING = EnumSet.of(ServiceStatus.PENDING, ServiceStatus.APPROVED, ServiceStatus.ASSIGNED);
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);

    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final ServiceRequestRepository requestRepository;
    private final SparePartRepository partRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final FeedbackRepository feedbackRepository;

    @Transactional(readOnly = true)
    public Admin admin() {
        List<ServiceRequest> requests = requestRepository.findAll();
        Map<String, Long> brands = vehicleRepository.findAll().stream()
                .collect(Collectors.groupingBy(Vehicle::getBrand, TreeMap::new, Collectors.counting()));
        return new Admin(
                userRepository.countByRole(Role.CUSTOMER), userRepository.countByRole(Role.MECHANIC),
                vehicleRepository.count(), requestRepository.countByStatus(ServiceStatus.PENDING),
                requestRepository.countByStatusIn(WORKING), requestRepository.countByStatus(ServiceStatus.COMPLETED),
                totalRevenue(), partRepository.countLowStock(),
                monthly(6), topServices(requests, 6),
                brands.entrySet().stream().map(e -> new Stat(e.getKey(), e.getValue(), null)).toList(),
                requests.stream().collect(Collectors.groupingBy(r -> r.getStatus().name(), TreeMap::new, Collectors.counting()))
                        .entrySet().stream().map(e -> new Stat(e.getKey(), e.getValue(), null)).toList());
    }

    @Transactional(readOnly = true)
    public DashboardDto.Customer customer() {
        Long uid = CurrentUser.id();
        List<ServiceRequest> mine = requestRepository.findByCustomer(uid);
        List<Invoice> pending = invoiceRepository.findByCustomer(uid).stream()
                .filter(i -> i.getPaymentStatus() == PaymentStatus.PENDING).toList();
        return new DashboardDto.Customer(
                vehicleRepository.countByOwner(uid),
                mine.stream().filter(r -> UPCOMING.contains(r.getStatus()) && !r.getAppointment().getAppointmentDate().isBefore(LocalDate.now())).count(),
                mine.stream().filter(r -> WORKING.contains(r.getStatus())).count(),
                mine.stream().filter(r -> r.getStatus() == ServiceStatus.COMPLETED).count(),
                pending.size(),
                pending.stream().map(Invoice::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    @Transactional(readOnly = true)
    public DashboardDto.Mechanic mechanic() {
        Long uid = CurrentUser.id();
        List<ServiceRequest> mine = requestRepository.findByMechanic(uid);
        return new DashboardDto.Mechanic(
                mine.stream().filter(r -> r.getStatus() != ServiceStatus.CANCELLED).count(),
                mine.stream().filter(r -> r.getStatus() == ServiceStatus.ASSIGNED).count(),
                mine.stream().filter(r -> WORKING.contains(r.getStatus())).count(),
                mine.stream().filter(r -> r.getStatus() == ServiceStatus.COMPLETED).count());
    }

    @Transactional(readOnly = true)
    public Report report() {
        List<ServiceRequest> completed = requestRepository.findAll().stream().filter(r -> r.getStatus() == ServiceStatus.COMPLETED).toList();
        List<Feedback> feedback = feedbackRepository.findAll();

        List<Stat> byType = completed.stream().collect(Collectors.groupingBy(r -> r.getService().getName())).entrySet().stream()
                .map(e -> new Stat(e.getKey(), e.getValue().size(), e.getValue().stream()
                        .filter(r -> r.getInvoice() != null).map(r -> r.getInvoice().getTotal()).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .sorted(Comparator.comparingLong(Stat::count).reversed()).toList();

        List<MechanicStat> mechanics = userRepository.findByRoleOrderByIdDesc(Role.MECHANIC).stream().map(m -> {
            long jobs = completed.stream().filter(r -> r.getMechanic() != null && r.getMechanic().getId().equals(m.getId())).count();
            double avg = feedback.stream().filter(f -> f.getServiceRequest().getMechanic() != null
                    && f.getServiceRequest().getMechanic().getId().equals(m.getId())).mapToInt(Feedback::getRating).average().orElse(0);
            return new MechanicStat(m.getName(), jobs, Math.round(avg * 10) / 10.0);
        }).toList();

        List<Stat> methods = paymentRepository.findAll().stream().collect(Collectors.groupingBy(p -> p.getMethod().name())).entrySet().stream()
                .map(e -> new Stat(e.getKey(), e.getValue().size(), e.getValue().stream().map(Payment::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)))
                .toList();

        BigDecimal outstanding = invoiceRepository.findByPaymentStatus(PaymentStatus.PENDING).stream()
                .map(Invoice::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        double avgRating = feedback.stream().mapToInt(Feedback::getRating).average().orElse(0);
        return new Report(monthly(12), byType, mechanics, methods, totalRevenue(), outstanding, Math.round(avgRating * 10) / 10.0);
    }

    // ---------------- helpers ----------------
    private BigDecimal totalRevenue() {
        return invoiceRepository.findByPaymentStatus(PaymentStatus.PAID).stream()
                .map(Invoice::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /** Revenue (paid invoices) and completed jobs for the last N months, oldest first. */
    private List<MonthPoint> monthly(int months) {
        Map<YearMonth, BigDecimal> revenue = invoiceRepository.findByPaymentStatus(PaymentStatus.PAID).stream()
                .collect(Collectors.groupingBy(i -> YearMonth.from(i.getInvoiceDate()),
                        Collectors.reducing(BigDecimal.ZERO, Invoice::getTotal, BigDecimal::add)));
        Map<YearMonth, Long> done = requestRepository.findAll().stream()
                .filter(r -> r.getStatus() == ServiceStatus.COMPLETED && r.getCompletedAt() != null)
                .collect(Collectors.groupingBy(r -> YearMonth.from(r.getCompletedAt()), Collectors.counting()));
        List<MonthPoint> points = new ArrayList<>();
        YearMonth now = YearMonth.now();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = now.minusMonths(i);
            points.add(new MonthPoint(ym.format(MONTH), revenue.getOrDefault(ym, BigDecimal.ZERO), done.getOrDefault(ym, 0L)));
        }
        return points;
    }

    private List<Stat> topServices(List<ServiceRequest> requests, int limit) {
        return requests.stream().filter(r -> r.getStatus() != ServiceStatus.CANCELLED)
                .collect(Collectors.groupingBy(r -> r.getService().getName(), Collectors.counting()))
                .entrySet().stream().sorted(Map.Entry.<String, Long>comparingByValue().reversed()).limit(limit)
                .map(e -> new Stat(e.getKey(), e.getValue(), null)).toList();
    }
}
