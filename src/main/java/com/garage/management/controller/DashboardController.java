package com.garage.management.controller;

import com.garage.management.dto.DashboardDto;
import com.garage.management.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {
    private final DashboardService dashboardService;

    @GetMapping("/dashboard/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardDto.Admin admin() { return dashboardService.admin(); }

    @GetMapping("/dashboard/customer")
    @PreAuthorize("hasRole('CUSTOMER')")
    public DashboardDto.Customer customer() { return dashboardService.customer(); }

    @GetMapping("/dashboard/mechanic")
    @PreAuthorize("hasRole('MECHANIC')")
    public DashboardDto.Mechanic mechanic() { return dashboardService.mechanic(); }

    @GetMapping("/reports/summary")
    @PreAuthorize("hasRole('ADMIN')")
    public DashboardDto.Report report() { return dashboardService.report(); }
}
