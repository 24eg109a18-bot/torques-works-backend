package com.garage.management.service;

import com.garage.management.dto.ServiceDto;
import com.garage.management.entity.GarageService;
import com.garage.management.entity.Role;
import com.garage.management.exception.BadRequestException;
import com.garage.management.exception.DuplicateResourceException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.repository.GarageServiceRepository;
import com.garage.management.util.CurrentUser;
import com.garage.management.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** The catalogue of services the garage offers (admin manages it). */
@Service
@RequiredArgsConstructor
public class ServiceCatalogService {
    private final GarageServiceRepository repository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<ServiceDto.Response> listActive() {
        return repository.findByActiveTrueOrderByNameAsc().stream().map(mapper::service).toList();
    }

    @Transactional(readOnly = true)
    public List<ServiceDto.Response> list() {
        List<GarageService> all = CurrentUser.role() == Role.ADMIN ? repository.findAll() : repository.findByActiveTrueOrderByNameAsc();
        return all.stream().map(mapper::service).toList();
    }

    @Transactional
    public ServiceDto.Response create(ServiceDto.Request req) {
        if (repository.existsByNameIgnoreCase(req.name().trim())) {
            throw new DuplicateResourceException("Service '" + req.name() + "' already exists");
        }
        GarageService s = new GarageService();
        apply(s, req);
        return mapper.service(repository.save(s));
    }

    @Transactional
    public ServiceDto.Response update(Long id, ServiceDto.Request req) {
        GarageService s = find(id);
        if (!s.getName().equalsIgnoreCase(req.name().trim()) && repository.existsByNameIgnoreCase(req.name().trim())) {
            throw new DuplicateResourceException("Service '" + req.name() + "' already exists");
        }
        apply(s, req);
        return mapper.service(s);
    }

    @Transactional
    public void delete(Long id) {
        GarageService s = find(id);
        try {
            repository.delete(s);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("This service is used by appointments. Mark it inactive instead of deleting.");
        }
    }

    private void apply(GarageService s, ServiceDto.Request r) {
        s.setName(r.name().trim());
        s.setDescription(r.description());
        s.setEstimatedCost(r.estimatedCost());
        s.setEstimatedDurationHours(r.estimatedDurationHours());
        s.setActive(r.active() == null || r.active());
    }

    private GarageService find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Service not found with id " + id));
    }
}
