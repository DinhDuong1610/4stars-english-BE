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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController("clientSubscriptionController")
@RequestMapping("/api/v1/subscriptions")
@Tag(name = "Client - Subscription Management API", description = "APIs for creating and managing user subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Operation(summary = "Create a new subscription for the current user", description = "User enrolls in a subscription plan. This creates a PENDING subscription, which must be paid for via a payment gateway.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subscription created successfully in PENDING state"),
            @ApiResponse(responseCode = "400", description = "The selected plan is not active"),
            @ApiResponse(responseCode = "409", description = "User already has an active subscription for this plan")
    })
    @PostMapping
    @ApiMessage("Create a new subscription (enroll in a course package)")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<SubscriptionResponseDTO> createSubscription(
            @Valid @RequestBody SubscriptionRequestDTO subscriptionRequestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {
        SubscriptionResponseDTO createdSubscription = subscriptionService.create(subscriptionRequestDTO);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdSubscription);
    }

    @Operation(summary = "Get my subscription by ID", description = "Retrieves details of a specific subscription belonging to the current user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved subscription"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Trying to access another user's subscription"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @GetMapping("/{id}")
    @ApiMessage("Fetch a subscription by its ID")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<SubscriptionResponseDTO> getSubscriptionById(@PathVariable long id)
            throws ResourceNotFoundException {
        SubscriptionResponseDTO subscription = subscriptionService.fetchSubscriptionById(id);
        return ResponseEntity.ok(subscription);
    }

    @Operation(summary = "Get my subscriptions", description = "Retrieves a paginated list of all subscriptions belonging to the current user.")
    @GetMapping
    @ApiMessage("Fetch all subscriptions for the current user")
    @PreAuthorize("hasPermission(null, null)")
    public ResponseEntity<ResultPaginationDTO<SubscriptionResponseDTO>> getCurrentUserSubscriptions(Pageable pageable)
            throws ResourceNotFoundException {
        ResultPaginationDTO<SubscriptionResponseDTO> result = subscriptionService.fetchUserSubscriptions(pageable);
        return ResponseEntity.ok(result);
    }

}
