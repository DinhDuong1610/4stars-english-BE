package com.fourstars.FourStars.controller.admin;

import org.springframework.data.domain.Pageable;
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

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    // Endpoint để admin xác nhận thanh toán (thường là webhook từ cổng thanh toán)
    @PostMapping("/confirm-payment/{subscriptionId}")
    // @PreAuthorize("hasAuthority('ROLE_ADMIN') or
    // hasAuthority('SYSTEM_PAYMENT_GATEWAY')") // Bảo vệ endpoint này
    public ResponseEntity<SubscriptionResponseDTO> confirmPayment(
            @PathVariable long subscriptionId,
            @RequestParam String transactionId,
            @RequestParam PaymentStatus status) throws ResourceNotFoundException {
        SubscriptionResponseDTO updatedSubscription = subscriptionService.confirmSubscriptionPayment(subscriptionId,
                transactionId, status);
        return ResponseEntity.ok(updatedSubscription);
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
    @ApiMessage("ADMIN: Fetch all subscriptions with pagination")
    // @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ResultPaginationDTO<SubscriptionResponseDTO>> getAllSubscriptionsAsAdmin(Pageable pageable) {
        ResultPaginationDTO<SubscriptionResponseDTO> result = subscriptionService
                .fetchAllSubscriptionsAsAdmin(pageable);
        return ResponseEntity.ok(result);
    }

}
