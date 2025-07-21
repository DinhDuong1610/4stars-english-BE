package com.fourstars.FourStars.controller.client;

import com.fourstars.FourStars.service.PaymentService;
import com.fourstars.FourStars.util.annotation.ApiMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.servlet.view.RedirectView;

@RestController
@RequestMapping("/api/v1/payments")
@Tag(name = "Payment API", description = "APIs for handling payment flows and webhooks")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Endpoint để client gọi và khởi tạo một yêu cầu thanh toán qua VNPay.
     * 
     * @param subscriptionId ID của gói đăng ký cần thanh toán.
     * @param request        Đối tượng HttpServletRequest để lấy thông tin cần thiết
     *                       như IP.
     * @return Một JSON chứa 'paymentUrl' để frontend chuyển hướng người dùng.
     */
    @Operation(summary = "Create VNPay Payment URL", description = "Initiates a payment process with VNPay for a specific subscription and returns a URL for the user to complete the payment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully created payment URL"),
            @ApiResponse(responseCode = "401", description = "User is not authenticated"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    @PostMapping("/vnpay/create/{subscriptionId}")
    @PreAuthorize("hasPermission(null, null)")
    @ApiMessage("Create a VNPay payment request URL")
    public ResponseEntity<Map<String, String>> createVNPayPayment(
            @PathVariable long subscriptionId,
            HttpServletRequest request) {
        try {
            String paymentUrl = paymentService.createVNPayPayment(subscriptionId, request);
            Map<String, String> response = new HashMap<>();
            response.put("paymentUrl", paymentUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Webhook for VNPay IPN", description = "Public endpoint for VNPay's server to send Instant Payment Notifications (IPN). This should not be called directly by users.", hidden = true)
    @GetMapping("/vnpay/ipn")
    @ApiMessage("Handle IPN (Instant Payment Notification) from VNPay")
    public ResponseEntity<Map<String, String>> handleVNPayIPN(@RequestParam Map<String, String> allParams) {
        try {
            Map<String, String> response = paymentService.handleVNPayIPN(allParams);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("RspCode", "99", "Message", "Unknown error"));
        }
    }

    // --- ENDPOINT XỬ LÝ KHI USER ĐƯỢC CHUYỂN HƯỚNG VỀ ---
    @Operation(summary = "User Return URL for VNPay", description = "The URL where VNPay redirects the user's browser after payment. This endpoint will then redirect to the frontend application.", hidden = true)
    @GetMapping("/vnpay/return")
    @ApiMessage("Handle user redirection from VNPay")
    public RedirectView handleVNPayReturn(@RequestParam Map<String, String> allParams)
            throws UnsupportedEncodingException {
        String vnp_ResponseCode = allParams.get("vnp_ResponseCode");
        String frontendReturnUrl = "http://localhost:5173/store"; // URL frontend của bạn

        // Cái này chỉ để test vì /vnpay/ipn chưa hoạt động, chưa deploy
        paymentService.handleVNPayTest(allParams);

        if ("00".equals(vnp_ResponseCode)) {
            // Thanh toán thành công -> chuyển hướng về trang thành công
            return new RedirectView(frontendReturnUrl + "?status=success");
        } else {
            // Thanh toán thất bại -> chuyển hướng về trang thất bại
            return new RedirectView(frontendReturnUrl + "?status=failed");
        }

        // return new RedirectView(frontendReturnUrl);
    }
}