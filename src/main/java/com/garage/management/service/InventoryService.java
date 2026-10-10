package com.garage.management.service;

import com.garage.management.dto.PartDto;
import com.garage.management.entity.SparePart;
import com.garage.management.exception.BadRequestException;
import com.garage.management.exception.DuplicateResourceException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.repository.SparePartRepository;
import com.garage.management.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Spare-part master data. Stock is reduced automatically by ServiceRequestService when a mechanic uses a part. */
@Service
@RequiredArgsConstructor
public class InventoryService {
    private final SparePartRepository repository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<PartDto.Response> list() {
        return repository.findAll().stream().map(mapper::part).toList();
    }

    @Transactional(readOnly = true)
    public List<PartDto.Response> lowStock() {
        return repository.findLowStock().stream().map(mapper::part).toList();
    }

    @Transactional
    public PartDto.Response create(PartDto.Request req) {
        if (repository.existsByPartNumberIgnoreCase(req.partNumber().trim())) {
            throw new DuplicateResourceException("Part number " + req.partNumber() + " already exists");
        }
        SparePart p = new SparePart();
        apply(p, req);
        return mapper.part(repository.save(p));
    }

    @Transactional
    public PartDto.Response update(Long id, PartDto.Request req) {
        SparePart p = find(id);
        if (!p.getPartNumber().equalsIgnoreCase(req.partNumber().trim()) && repository.existsByPartNumberIgnoreCase(req.partNumber().trim())) {
            throw new DuplicateResourceException("Part number " + req.partNumber() + " already exists");
        }
        apply(p, req);
        return mapper.part(p);
    }

    @Transactional
    public void delete(Long id) {
        SparePart p = find(id);
        try {
            repository.delete(p);
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("This part has been used in service jobs and cannot be deleted");
        }
    }

    private void apply(SparePart p, PartDto.Request r) {
        p.setName(r.name().trim());
        p.setPartNumber(r.partNumber().trim());
        p.setBrand(r.brand());
        p.setCategory(r.category());
        p.setPrice(r.price());
        p.setQuantity(r.quantity());
        p.setMinStockLevel(r.minStockLevel());
    }

    private SparePart find(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Spare part not found with id " + id));
    }
}
