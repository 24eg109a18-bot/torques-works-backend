package com.garage.management.config;

import com.garage.management.entity.*;
import com.garage.management.repository.*;
import com.garage.management.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Inserts demo data on the very first start (when the users table is empty).
 * Passwords are hashed with BCrypt, exactly like real registrations.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {
    private final UserRepository userRepo;
    private final VehicleRepository vehicleRepo;
    private final GarageServiceRepository serviceRepo;
    private final SparePartRepository partRepo;
    private final AppointmentRepository appointmentRepo;
    private final ServiceRequestRepository requestRepo;
    private final PaymentRepository paymentRepo;
    private final FeedbackRepository feedbackRepo;
    private final InvoiceService invoiceService;
    private final PasswordEncoder encoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepo.count() > 0) return;

        // ---------- users ----------
        user("System Admin", "admin@garage.com", "Admin@123", "9876543210", "Garage HQ, Hyderabad", Role.ADMIN, null);
        User ravi = user("Ravi Kumar", "ravi@garage.com", "Mechanic@123", "9000000001", "Hyderabad", Role.MECHANIC, "Engine & Transmission");
        User suresh = user("Suresh Reddy", "suresh@garage.com", "Mechanic@123", "9000000002", "Hyderabad", Role.MECHANIC, "Brakes & Suspension");
        User anil = user("Anil Sharma", "anil@garage.com", "Mechanic@123", "9000000003", "Secunderabad", Role.MECHANIC, "AC & Electricals");
        User rahul = user("Rahul Verma", "rahul@example.com", "Customer@123", "9123456780", "Madhapur, Hyderabad", Role.CUSTOMER, null);
        User priya = user("Priya Nair", "priya@example.com", "Customer@123", "9123456781", "Gachibowli, Hyderabad", Role.CUSTOMER, null);
        User amit = user("Amit Singh", "amit@example.com", "Customer@123", "9123456782", "Kukatpally, Hyderabad", Role.CUSTOMER, null);

        // ---------- vehicles ----------
        LocalDate today = LocalDate.now();
        Vehicle swift = vehicle(rahul, "TS09EB1234", "Maruti Suzuki", "Swift", "VXi", 2019, "Petrol", 42000, "White", today.plusMonths(8), "RC-TS-0001");
        Vehicle creta = vehicle(rahul, "TS08FG5678", "Hyundai", "Creta", "SX", 2021, "Diesel", 28000, "Black", today.plusMonths(3), "RC-TS-0002");
        Vehicle city = vehicle(priya, "AP28CD4321", "Honda", "City", "ZX", 2018, "Petrol", 61000, "Silver", today.plusMonths(1), "RC-AP-0003");
        Vehicle nexon = vehicle(amit, "TS10HJ7788", "Tata", "Nexon", "XZ+", 2022, "Petrol", 15000, "Blue", today.plusMonths(10), "RC-TS-0004");
        Vehicle innova = vehicle(amit, "KA05MN9090", "Toyota", "Innova Crysta", "GX", 2020, "Diesel", 55000, "Grey", today.plusMonths(5), "RC-KA-0005");

        // ---------- services ----------
        GarageService general = service("General Service", "Complete periodic service: oil, filters, fluids and 40-point check.", "2999", 3);
        GarageService oil = service("Oil Change", "Engine oil and oil filter replacement.", "1499", 1);
        GarageService brake = service("Brake Service", "Brake pad / disc inspection, cleaning and replacement.", "3499", 3);
        GarageService ac = service("AC Service", "AC gas refill, filter cleaning and cooling performance check.", "2499", 2);
        GarageService engine = service("Engine Service", "Engine diagnostics, tuning and repair.", "8999", 8);
        GarageService align = service("Wheel Alignment", "Computerised wheel alignment and balancing.", "899", 1);
        GarageService battery = service("Battery Replacement", "Battery testing and replacement with warranty.", "1200", 1);
        service("Tyre Replacement", "Tyre fitting, balancing and disposal of old tyres.", "800", 2);
        service("Denting & Painting", "Body denting, colour matching and painting.", "12000", 24);
        GarageService inspection = service("Full Inspection", "Complete vehicle inspection with detailed report.", "999", 2);

        // ---------- spare parts ----------
        SparePart engineOil = part("Engine Oil 5W-30 (1L)", "OIL-5W30-1L", "Castrol", "Lubricants", "650", 40, 10);
        SparePart oilFilter = part("Oil Filter", "FLT-OIL-001", "Bosch", "Filters", "250", 30, 10);
        SparePart airFilter = part("Air Filter", "FLT-AIR-002", "Bosch", "Filters", "350", 25, 8);
        SparePart brakePad = part("Brake Pad Set (Front)", "BRK-PAD-F01", "Bosch", "Brakes", "1800", 12, 5);
        part("Brake Disc", "BRK-DSC-002", "Brembo", "Brakes", "2200", 4, 5);            // low stock
        part("Spark Plug", "IGN-SPK-001", "NGK", "Ignition", "300", 40, 10);
        part("AC Compressor", "AC-CMP-001", "Denso", "AC", "9500", 2, 3);               // low stock
        SparePart acGas = part("AC Gas R134a", "AC-GAS-134", "Generic", "AC", "500", 15, 5);
        part("Car Battery 12V 45Ah", "BAT-12V-45", "Exide", "Electrical", "4800", 6, 3);
        part("Wiper Blade", "WPR-BLD-001", "Bosch", "Accessories", "450", 20, 5);
        part("Coolant (1L)", "CLT-001", "Shell", "Fluids", "380", 18, 6);
        part("Clutch Plate", "CLT-PLT-002", "Valeo", "Transmission", "3200", 3, 4);      // low stock

        // ---------- completed jobs (history, revenue and charts) ----------
        ServiceRequest r1 = request(rahul, swift, general, today.minusMonths(5), 10, "Due for 40,000 km service", ServiceStatus.COMPLETED, ravi);
        use(r1, engineOil, 4); use(r1, oilFilter, 1); use(r1, airFilter, 1);
        finish(r1, "Oil and filters replaced, all fluids topped up.", "800", today.minusMonths(5), PaymentMethod.UPI, 5, "Quick and clean work. Very happy!");

        ServiceRequest r2 = request(priya, city, brake, today.minusMonths(4), 11, "Squeaking noise while braking", ServiceStatus.COMPLETED, suresh);
        use(r2, brakePad, 1);
        finish(r2, "Front pads worn out, replaced. Discs resurfaced.", "600", today.minusMonths(4), PaymentMethod.CARD, 4, "Noise is gone. Good service.");

        ServiceRequest r3 = request(amit, nexon, ac, today.minusMonths(3), 14, "AC is not cooling properly", ServiceStatus.COMPLETED, anil);
        use(r3, acGas, 1);
        finish(r3, "Low gas. Refilled and leak-tested.", "700", today.minusMonths(3), PaymentMethod.CASH, 5, "Cooling is great now.");

        ServiceRequest r4 = request(rahul, creta, oil, today.minusMonths(2), 9, "Oil change", ServiceStatus.COMPLETED, ravi);
        use(r4, engineOil, 5); use(r4, oilFilter, 1);
        finish(r4, "Routine oil change done.", "300", today.minusMonths(2), PaymentMethod.ONLINE, 4, "On time.");

        ServiceRequest r5 = request(amit, innova, inspection, today.minusMonths(1), 15, "Pre-trip inspection", ServiceStatus.COMPLETED, suresh);
        finish(r5, "All fine. Minor tyre wear on rear left.", "500", today.minusMonths(1), PaymentMethod.UPI, 5, "Detailed report, thank you.");

        ServiceRequest r6 = request(priya, city, align, today.minusDays(10), 12, "Car pulls to the left", ServiceStatus.COMPLETED, anil);
        finish(r6, "Alignment and balancing completed.", "0", today.minusDays(10), null, null, null);   // invoice left unpaid

        // ---------- open jobs in every other status ----------
        request(rahul, swift, ac, nextOpenDay(today, 3), 10, "AC smells bad", ServiceStatus.PENDING, null);
        request(priya, city, engine, nextOpenDay(today, 4), 11, "Engine makes ticking noise", ServiceStatus.APPROVED, null);
        request(amit, innova, battery, nextOpenDay(today, 2), 9, "Car does not start in the morning", ServiceStatus.ASSIGNED, anil);

        ServiceRequest inProgress = request(rahul, creta, brake, today, 16, "Brake pedal feels soft", ServiceStatus.IN_PROGRESS, suresh);
        inProgress.setInspectionNotes("Brake fluid is low and front pads are 80% worn.");
        inProgress.setLabourCharge(new BigDecimal("500"));
        inProgress.setEstimatedCompletionDate(today.plusDays(1));
        use(inProgress, brakePad, 1);

        ServiceRequest waiting = request(amit, nexon, general, today.minusDays(1), 10, "Regular service", ServiceStatus.WAITING_FOR_PARTS, ravi);
        waiting.setInspectionNotes("Clutch plate worn. Waiting for replacement stock from supplier.");
        waiting.setEstimatedCompletionDate(today.plusDays(3));
        request(rahul, swift, oil, today.minusDays(20), 10, "Changed my mind", ServiceStatus.CANCELLED, null);
    }

    // ---------------- helper methods ----------------
    private User user(String name, String email, String pwd, String phone, String address, Role role, String spec) {
        User u = new User();
        u.setName(name); u.setEmail(email); u.setPassword(encoder.encode(pwd));
        u.setPhone(phone); u.setAddress(address); u.setRole(role); u.setSpecialization(spec);
        return userRepo.save(u);
    }

    private Vehicle vehicle(User owner, String reg, String brand, String model, String variant, int year, String fuel,
                            int km, String color, LocalDate insurance, String rc) {
        Vehicle v = new Vehicle();
        v.setOwner(owner); v.setRegistrationNumber(reg); v.setBrand(brand); v.setModel(model); v.setVariant(variant);
        v.setManufacturingYear(year); v.setFuelType(fuel); v.setCurrentKm(km); v.setColor(color);
        v.setInsuranceExpiry(insurance); v.setRcNumber(rc);
        return vehicleRepo.save(v);
    }

    private GarageService service(String name, String desc, String cost, int hours) {
        GarageService s = new GarageService();
        s.setName(name); s.setDescription(desc); s.setEstimatedCost(new BigDecimal(cost)); s.setEstimatedDurationHours(hours);
        return serviceRepo.save(s);
    }

    private SparePart part(String name, String number, String brand, String category, String price, int qty, int min) {
        SparePart p = new SparePart();
        p.setName(name); p.setPartNumber(number); p.setBrand(brand); p.setCategory(category);
        p.setPrice(new BigDecimal(price)); p.setQuantity(qty); p.setMinStockLevel(min);
        return partRepo.save(p);
    }

    private LocalDate nextOpenDay(LocalDate from, int days) {
        LocalDate d = from.plusDays(days);
        return d.getDayOfWeek() == java.time.DayOfWeek.SUNDAY ? d.plusDays(1) : d;
    }

    private ServiceRequest request(User customer, Vehicle v, GarageService s, LocalDate date, int hour, String problem,
                                   ServiceStatus status, User mechanic) {
        Appointment a = new Appointment();
        a.setCustomer(customer); a.setVehicle(v); a.setService(s);
        a.setAppointmentDate(date); a.setAppointmentTime(LocalTime.of(hour, 0));
        a.setProblemDescription(problem); a.setStatus(status);
        a.setCreatedAt(date.minusDays(2).atTime(9, 30));
        a = appointmentRepo.save(a);

        ServiceRequest r = new ServiceRequest();
        r.setAppointment(a); r.setVehicle(v); r.setService(s); r.setMechanic(mechanic); r.setStatus(status);
        r.setCreatedAt(a.getCreatedAt());
        return requestRepo.save(r);
    }

    private void use(ServiceRequest r, SparePart p, int qty) {
        p.setQuantity(p.getQuantity() - qty);
        UsedPart u = new UsedPart();
        u.setServiceRequest(r); u.setSparePart(p); u.setQuantity(qty); u.setUnitPrice(p.getPrice());
        r.getUsedParts().add(u);
    }

    /** Completes a job: notes, labour, invoice, optional payment and feedback. */
    private void finish(ServiceRequest r, String notes, String labour, LocalDate date, PaymentMethod method,
                        Integer rating, String comment) {
        r.setInspectionNotes(notes);
        r.setLabourCharge(new BigDecimal(labour));
        r.setCompletedAt(date.atTime(17, 0));
        Invoice invoice = invoiceService.generateFor(r);
        invoice.setInvoiceDate(date);

        if (method != null) {
            Payment p = new Payment();
            p.setInvoice(invoice); p.setAmount(invoice.getTotal()); p.setMethod(method);
            p.setPaymentDate(date.atTime(18, 0));
            p.setTransactionId((method == PaymentMethod.CASH ? "CASH-" : "TXN-") + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
            p.setStatus(PaymentStatus.PAID);
            paymentRepo.save(p);
            invoice.setPaymentStatus(PaymentStatus.PAID);
        }
        if (rating != null) {
            Feedback f = new Feedback();
            f.setServiceRequest(r); f.setCustomer(r.getVehicle().getOwner()); f.setRating(rating);
            f.setComment(comment); f.setCreatedAt(LocalDateTime.of(date, LocalTime.of(19, 0)));
            feedbackRepo.save(f);
        }
    }
}
