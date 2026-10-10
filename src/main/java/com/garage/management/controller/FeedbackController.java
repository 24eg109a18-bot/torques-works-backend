package com.garage.management.controller;

import com.garage.management.dto.FeedbackDto;
import com.garage.management.service.FeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedback")
@RequiredArgsConstructor
public class FeedbackController {
    private final FeedbackService feedbackService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','CUSTOMER')")
    public List<FeedbackDto.Response> list() { return feedbackService.list(); }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<FeedbackDto.Response> submit(@Valid @RequestBody FeedbackDto.Request req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackService.submit(req));
    }
}
