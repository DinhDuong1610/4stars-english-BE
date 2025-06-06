package com.fourstars.FourStars.controller.client;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.subscription.SubscriptionRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.subscription.SubscriptionResponseDTO;
import com.fourstars.FourStars.service.SubscriptionService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController("clientSubscriptionController")
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    @ApiMessage("Create a new subscription (enroll in a course package)")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionResponseDTO> createSubscription(
            @Valid @RequestBody SubscriptionRequestDTO subscriptionRequestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        SubscriptionResponseDTO createdSubscription = subscriptionService.create(subscriptionRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubscription);
    }

    @GetMapping("/{id}")
    @ApiMessage("Fetch a subscription by its ID")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<SubscriptionResponseDTO> getSubscriptionById(@PathVariable long id)
            throws ResourceNotFoundException {
        SubscriptionResponseDTO subscription = subscriptionService.fetchSubscriptionById(id);
        return ResponseEntity.ok(subscription);
    }

    @GetMapping
    @ApiMessage("Fetch all subscriptions for the current user")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ResultPaginationDTO<SubscriptionResponseDTO>> getCurrentUserSubscriptions(Pageable pageable)
            throws ResourceNotFoundException {
        ResultPaginationDTO<SubscriptionResponseDTO> result = subscriptionService.fetchUserSubscriptions(pageable);
        return ResponseEntity.ok(result);
    }

}
