package com.garage.management.service;

import com.garage.management.dto.FeedbackDto;
import com.garage.management.entity.*;
import com.garage.management.exception.BadRequestException;
import com.garage.management.exception.DuplicateResourceException;
import com.garage.management.exception.ResourceNotFoundException;
import com.garage.management.exception.UnauthorizedException;
import com.garage.management.repository.FeedbackRepository;
import com.garage.management.repository.ServiceRequestRepository;
import com.garage.management.repository.UserRepository;
import com.garage.management.util.CurrentUser;
import com.garage.management.util.EntityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedbackService {
    private final FeedbackRepository feedbackRepository;
    private final ServiceRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final EntityMapper mapper;

    @Transactional(readOnly = true)
    public List<FeedbackDto.Response> list() {
        List<Feedback> list = CurrentUser.role() == Role.ADMIN
                ? feedbackRepository.findAllByOrderByIdDesc()
                : feedbackRepository.findByCustomerIdOrderByIdDesc(CurrentUser.id());
        return list.stream().map(mapper::feedback).toList();
    }

    @Transactional
    public FeedbackDto.Response submit(FeedbackDto.Request req) {
        ServiceRequest r = requestRepository.findById(req.serviceRequestId())
                .orElseThrow(() -> new ResourceNotFoundException("Service request not found"));
        if (!r.getVehicle().getOwner().getId().equals(CurrentUser.id())) throw new UnauthorizedException("You can only review your own services");
        if (r.getStatus() != ServiceStatus.COMPLETED) throw new BadRequestException("Feedback can only be given after the service is completed");
        if (feedbackRepository.existsByServiceRequestId(r.getId())) throw new DuplicateResourceException("You have already submitted feedback for this service");

        Feedback f = new Feedback();
        f.setServiceRequest(r);
        f.setCustomer(userRepository.findById(CurrentUser.id()).orElseThrow());
        f.setRating(req.rating());
        f.setComment(req.comment());
        f.setCreatedAt(LocalDateTime.now());
        return mapper.feedback(feedbackRepository.save(f));
    }
}
