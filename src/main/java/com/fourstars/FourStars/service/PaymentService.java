package com.fourstars.FourStars.service;

import com.fourstars.FourStars.config.VNPayConfig;
import com.fourstars.FourStars.domain.Subscription;
import com.fourstars.FourStars.repository.SubscriptionRepository;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fourstars.FourStars.util.constant.PaymentStatus;
import org.springframework.transaction.annotation.Transactional;
import java.io.UnsupportedEncodingException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.url}")
    private String vnpUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    private final SubscriptionRepository subscriptionRepository;

    public PaymentService(SubscriptionRepository subscriptionRepository) {
        this.subscriptionRepository = subscriptionRepository;
    }

    /**
     * Phương thức này chịu trách nhiệm tạo ra một URL thanh toán VNPay hợp lệ
     * chứa tất cả các tham số cần thiết và chữ ký bảo mật.
     * 
     * @param subscriptionId ID của gói đăng ký đang chờ thanh toán.
     * @param request        Đối tượng HttpServletRequest để lấy địa chỉ IP của
     *                       người dùng.
     * @return Một chuỗi String là URL thanh toán hoàn chỉnh.
     */
    public String createVNPayPayment(long subscriptionId, HttpServletRequest request) {
        logger.info("Initiating VNPay payment creation for subscription ID: {}", subscriptionId);

        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription not found with id: " + subscriptionId));

        long amount = subscription.getPlan().getPrice().longValue() * 100;

        // Tạo mã tham chiếu giao dịch duy nhất cho mỗi lần thanh toán
        String vnp_TxnRef = subscription.getId() + "_" + VNPayConfig.getRandomNumber(8);

        logger.debug("Building VNPay params: amount={}, vnp_TxnRef={}", amount, vnp_TxnRef);

        // Lấy địa chỉ IP của người dùng
        String vnp_IpAddr = VNPayConfig.getIpAddress(request);

        // Build các tham số cần thiết cho URL
        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", VNPayConfig.vnp_Version);
        vnp_Params.put("vnp_Command", VNPayConfig.vnp_Command);
        vnp_Params.put("vnp_TmnCode", tmnCode);
        vnp_Params.put("vnp_Amount", String.valueOf(amount));
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", "Thanh toán gói học: " + subscription.getPlan().getName());
        vnp_Params.put("vnp_OrderType", VNPayConfig.vnp_OrderType);
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", returnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);

        // Set thời gian tạo và hết hạn giao dịch
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnp_CreateDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        cld.add(Calendar.MINUTE, 15); // Thời gian hết hạn là 15 phút
        String vnp_ExpireDate = formatter.format(cld.getTime());
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        // Sắp xếp các tham số theo thứ tự alphabet
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);

        // Tạo chuỗi dữ liệu để băm (hash)
        StringBuilder hashData = new StringBuilder();
        // Tạo chuỗi query cho URL
        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (!fieldValue.isEmpty())) {
                // Build hash data
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                // Build query
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII));
                if (itr.hasNext()) {
                    query.append('&');
                    hashData.append('&');
                }
            }
        }

        String queryUrl = query.toString();
        logger.debug("Raw query string for hashing: {}", hashData.toString());

        // Tạo chữ ký bảo mật
        String vnp_SecureHash = VNPayConfig.hmacSHA512(hashSecret, hashData.toString());
        queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;

        logger.info("Successfully created VNPay payment URL for subscription ID: {}", subscriptionId);

        return vnpUrl + "?" + queryUrl;
    }

    @Transactional
    public Map<String, String> handleVNPayIPN(Map<String, String> vnp_Params) throws UnsupportedEncodingException {
        String orderInfo = vnp_Params.get("vnp_OrderInfo");
        logger.info("Received IPN notification from VNPay for order: {}", orderInfo);
        logger.debug("IPN raw params: {}", vnp_Params);

        // Lấy hash bí mật từ tham số trả về
        String vnp_SecureHash = vnp_Params.remove("vnp_SecureHash");

        // Sắp xếp các tham số còn lại và tạo chuỗi dữ liệu để kiểm tra chữ ký
        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                if (itr.hasNext()) {
                    hashData.append('&');
                }
            }
        }

        // Tạo chữ ký từ dữ liệu nhận được
        String mySecureHash = VNPayConfig.hmacSHA512(hashSecret, hashData.toString());
        logger.debug("Calculated signature: {}. Signature from VNPay: {}", mySecureHash, vnp_SecureHash);

        Map<String, String> response = new HashMap<>();

        // 1. KIỂM TRA CHỮ KÝ
        if (mySecureHash.equals(vnp_SecureHash)) {
            logger.info("IPN signature is valid for order: {}", orderInfo);

            // Lấy ID subscription từ mã giao dịch
            String vnp_TxnRef = vnp_Params.get("vnp_TxnRef");
            long subscriptionId = Long.parseLong(vnp_TxnRef.split("_")[0]);

            Subscription subscription = subscriptionRepository.findById(subscriptionId).orElse(null);

            // 2. KIỂM TRA ĐƠN HÀNG (SUBSCRIPTION)
            if (subscription != null) {
                // 3. KIỂM TRA TRẠNG THÁI ĐÃ UPDATE CHƯA
                if (subscription.getPaymentStatus() != PaymentStatus.PAID) {
                    // 4. KIỂM TRA SỐ TIỀN
                    long amountFromVNPay = Long.parseLong(vnp_Params.get("vnp_Amount")) / 100;
                    long amountFromDB = subscription.getPlan().getPrice().longValue();

                    if (amountFromVNPay == amountFromDB) {
                        // 5. KIỂM TRA MÃ PHẢN HỒI CỦA VNPAY
                        if ("00".equals(vnp_Params.get("vnp_ResponseCode"))) {
                            logger.info("Payment successful for subscription ID: {}. Updating status to PAID.",
                                    subscriptionId);
                            // Giao dịch thành công -> Cập nhật trạng thái
                            subscription.setPaymentStatus(PaymentStatus.PAID);
                            subscription.setActive(true);
                            subscription.setTransactionId(vnp_Params.get("vnp_TransactionNo"));
                            subscriptionRepository.save(subscription);

                            response.put("RspCode", "00");
                            response.put("Message", "Confirm Success");
                        } else {
                            logger.warn("Payment failed for subscription ID: {}. VNPay response code: {}",
                                    subscriptionId, vnp_Params.get("vnp_ResponseCode"));

                            // Giao dịch thất bại
                            subscription.setPaymentStatus(PaymentStatus.FAILED);
                            subscriptionRepository.save(subscription);

                            response.put("RspCode", "01"); // Hoặc mã lỗi khác
                            response.put("Message", "Confirm Failed");
                        }
                    } else {
                        logger.error("IPN amount mismatch for subscription ID: {}. Expected: {}, Received: {}",
                                subscriptionId, amountFromDB, amountFromVNPay);

                        response.put("RspCode", "04");
                        response.put("Message", "Invalid Amount");
                    }
                } else {
                    logger.warn("IPN received for an already confirmed subscription ID: {}", subscriptionId);

                    response.put("RspCode", "02");
                    response.put("Message", "Order already confirmed");
                }
            } else {
                logger.error("IPN received for non-existent subscription. vnp_TxnRef: {}", vnp_TxnRef);

                response.put("RspCode", "01");
                response.put("Message", "Order not Found");
            }
        } else {
            logger.error("INVALID SIGNATURE for IPN notification. Order info: {}", orderInfo);

            response.put("RspCode", "97");
            response.put("Message", "Invalid Signature");
        }
        logger.info("Finished processing IPN for order: {}. Responding with code: {}", orderInfo,
                response.get("RspCode"));

        return response;
    }

    @Transactional
    public boolean handleVNPayTest(Map<String, String> vnp_Params)
            throws UnsupportedEncodingException {

        String vnp_TxnRef = vnp_Params.get("vnp_TxnRef");
        long subscriptionId = Long.parseLong(vnp_TxnRef.split("_")[0]);

        Subscription subscription = subscriptionRepository.findById(subscriptionId).orElse(null);
        if (subscription != null) {
            if (subscription.getPaymentStatus() != PaymentStatus.PAID) {
                logger.info("Payment successful for subscription ID: {}. Updating status to PAID.",
                        subscriptionId);
                subscription.setPaymentStatus(PaymentStatus.PAID);
                subscription.setActive(true);
                subscription.setTransactionId(vnp_Params.get("vnp_TransactionNo"));
                subscriptionRepository.save(subscription);

                return true;
            } else {
                logger.warn("Payment failed for subscription ID: {}. VNPay response code: {}",
                        subscriptionId, vnp_Params.get("vnp_ResponseCode"));

                subscription.setPaymentStatus(PaymentStatus.FAILED);
                subscriptionRepository.save(subscription);

                return false;
            }
        }
        return false;
    }

}
