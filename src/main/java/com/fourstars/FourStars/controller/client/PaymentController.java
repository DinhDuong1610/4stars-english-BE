package com.fourstars.FourStars.controller.client;

import com.fourstars.FourStars.service.PaymentService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
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
    @PostMapping("/vnpay/create/{subscriptionId}")
    @PreAuthorize("isAuthenticated()")
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
}