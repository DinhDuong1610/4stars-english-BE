package com.fourstars.FourStars.controller.admin;

import java.time.LocalDate;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.subscription.SubscriptionResponseDTO;
import com.fourstars.FourStars.service.SubscriptionService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.constant.PaymentStatus;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin - Subscription Management API", description = "APIs for creating and managing user subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Operation(summary = "Confirm a subscription payment", description = "Manually updates the payment status of a subscription. Can also be used as a webhook target.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Payment status updated successfully"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @PostMapping("/confirm-payment/{subscriptionId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<SubscriptionResponseDTO> confirmPayment(
            @PathVariable long subscriptionId,
            @RequestParam String transactionId,
            @RequestParam PaymentStatus status) throws ResourceNotFoundException {
        SubscriptionResponseDTO updatedSubscription = subscriptionService.confirmSubscriptionPayment(subscriptionId,
                transactionId, status);
        return ResponseEntity.ok(updatedSubscription);
    }

    @Operation(summary = "Get a subscription by ID", description = "Allows an admin to retrieve any subscription by its ID.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch a subscription by its ID")
    public ResponseEntity<SubscriptionResponseDTO> getSubscriptionById(@PathVariable long id)
            throws ResourceNotFoundException {
        SubscriptionResponseDTO subscription = subscriptionService.fetchSubscriptionById(id);
        return ResponseEntity.ok(subscription);
    }

    @Operation(summary = "Get all subscriptions", description = "Retrieves a paginated list of all subscriptions in the system.")
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("ADMIN: Fetch all subscriptions with pagination")
    public ResponseEntity<ResultPaginationDTO<SubscriptionResponseDTO>> getAllSubscriptionsAsAdmin(Pageable pageable,
            @Parameter(description = "Filter by User ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "Filter by Plan ID") @RequestParam(required = false) Long planId,
            @Parameter(description = "Filter by Payment Status (PENDING, PAID, FAILED)") @RequestParam(required = false) PaymentStatus paymentStatus,
            @Parameter(description = "Filter by start date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "Filter by end date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        ResultPaginationDTO<SubscriptionResponseDTO> result = subscriptionService
                .fetchAllSubscriptionsAsAdmin(pageable, userId, planId, paymentStatus, startDate, endDate);
        return ResponseEntity.ok(result);
    }

}
