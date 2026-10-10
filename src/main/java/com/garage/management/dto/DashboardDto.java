package com.garage.management.dto;

import java.math.BigDecimal;
import java.util.List;

public class DashboardDto {
    public record Stat(String name, long count, BigDecimal amount) {}

    public record MonthPoint(String month, BigDecimal revenue, long completed) {}

    public record MechanicStat(String name, long completedJobs, double averageRating) {}

    public record Admin(long totalCustomers, long totalMechanics, long totalVehicles, long pendingAppointments,
                        long activeServices, long completedServices, BigDecimal totalRevenue, long lowStockParts,
                        List<MonthPoint> monthly, List<Stat> topServices, List<Stat> vehicleBrands,
                        List<Stat> statusBreakdown) {}

    public record Customer(long myVehicles, long upcomingAppointments, long activeServices, long previousServices,
                           long pendingPayments, BigDecimal pendingAmount) {}

    public record Mechanic(long assignedJobs, long pendingJobs, long inProgressJobs, long completedJobs) {}

    public record Report(List<MonthPoint> monthly, List<Stat> servicesByType, List<MechanicStat> mechanics,
                         List<Stat> paymentMethods, BigDecimal totalRevenue, BigDecimal outstandingAmount,
                         double averageRating) {}
}
