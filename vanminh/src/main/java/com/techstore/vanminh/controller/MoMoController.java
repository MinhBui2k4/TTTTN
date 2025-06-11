package com.techstore.vanminh.controller;

import com.techstore.vanminh.constant.PaymentGatewayConstant;
import com.techstore.vanminh.dto.MoMoRequestDto;
import com.techstore.vanminh.dto.response.SimpleResponse;
import com.techstore.vanminh.dto.response.OrderResponseDTO;
import com.techstore.vanminh.entity.Order;
import com.techstore.vanminh.entity.PaymentTransaction;
import com.techstore.vanminh.repository.OrderRepository;
import com.techstore.vanminh.repository.PaymentTransactionRepository;
import com.techstore.vanminh.service.OrderService;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/payment/momo")
public class MoMoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoMoController.class);

    private static final int MAX_RESPONSE_LENGTH = 65535; // Max length for TEXT column (fallback)

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentTransactionRepository paymentTransactionRepository;

    @PostMapping("/create")
    public ResponseEntity<SimpleResponse<Map<String, String>>> createPayment(@RequestBody MoMoRequestDto request) {
        try {
            LOGGER.info("Creating MoMo payment for order: {}", request.getOrderId());
            validateRequest(request);

            // Verify order exists and is in ORDERED status
            Long orderId = Long.parseLong(request.getOrderId());
            OrderResponseDTO orderDTO = orderService.getOrderById(orderId);
            if (!orderDTO.getStatus().equals("ORDERED")) {
                SimpleResponse<Map<String, String>> response = new SimpleResponse<>();
                response.setMessage("Order is not in ORDERED status");
                return ResponseEntity.badRequest().body(response);
            }

            // Fetch Order entity
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));

            // Check for existing pending transaction
            paymentTransactionRepository.findByOrderIdAndStatus(orderId, "PENDING")
                    .ifPresent(tx -> {
                        throw new IllegalStateException(
                                "A pending MoMo payment exists for this order. Please complete or cancel it.");
                    });

            // Use orderId as transactionId
            String transactionId = String.valueOf(orderId);
            // Generate unique momoOrderId for MoMo API
            String momoOrderId = "MOMO-" + orderId + "-" + UUID.randomUUID().toString().substring(0, 8);
            String requestId = PaymentGatewayConstant.PARTNER_CODE + System.currentTimeMillis();
            String amount = String.valueOf(request.getAmount().longValue());
            String orderInfo = request.getOrderInfo() != null ? request.getOrderInfo()
                    : "Thanh toán đơn hàng #" + orderId;

            // Save payment transaction
            PaymentTransaction transaction = new PaymentTransaction();
            transaction.setOrder(order);
            transaction.setTransactionId(transactionId);
            transaction.setMomoOrderId(momoOrderId);
            transaction.setPaymentMethod("MOMO");
            transaction.setStatus("PENDING");
            transaction.setAmount(request.getAmount());
            transaction.setCreatedAt(LocalDateTime.now());
            paymentTransactionRepository.save(transaction);

            Map<String, String> momoParams = buildMoMoParams(requestId, momoOrderId, amount, orderInfo);
            String signature = generateSignature(momoParams);
            momoParams.put("signature", signature);

            String response = sendPaymentRequest(momoParams);
            JSONObject responseJson = new JSONObject(response);

            SimpleResponse<Map<String, String>> simpleResponse = new SimpleResponse<>();
            if (responseJson.has("payUrl")) {
                // Truncate response if necessary
                transaction.setMoMoResponse(truncateString(response, MAX_RESPONSE_LENGTH));
                paymentTransactionRepository.save(transaction);

                Map<String, String> result = new HashMap<>();
                result.put("payUrl", responseJson.getString("payUrl"));
                result.put("originalOrderId", request.getOrderId());
                result.put("momoOrderId", momoOrderId);
                simpleResponse.setContent(result);
                simpleResponse.setMessage("Payment URL created successfully");
                return ResponseEntity.ok(simpleResponse);
            } else {
                transaction.setStatus("FAILED");
                transaction.setMoMoResponse(truncateString(response, MAX_RESPONSE_LENGTH));
                paymentTransactionRepository.save(transaction);

                LOGGER.error("MoMo payment creation failed: {}", responseJson.optString("message"));
                simpleResponse.setMessage(responseJson.optString("message", "Failed to create payment URL"));
                return ResponseEntity.badRequest().body(simpleResponse);
            }
        } catch (IllegalStateException e) {
            LOGGER.error("MoMo payment creation failed: {}", e.getMessage());
            SimpleResponse<Map<String, String>> response = new SimpleResponse<>();
            response.setMessage(e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            LOGGER.error("Failed to create MoMo payment: {}", e.getMessage(), e);
            SimpleResponse<Map<String, String>> response = new SimpleResponse<>();
            response.setMessage("Failed to create payment: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/callback")
    public ResponseEntity<Map<String, String>> handleMoMoCallback(@RequestBody Map<String, String> momoParams) {
        LOGGER.info("Received MoMo callback: {}", momoParams);
        try {
            if (!validateCallbackSignature(momoParams)) {
                LOGGER.error("Invalid signature in MoMo callback");
                return ResponseEntity.badRequest().body(Map.of("status", "failed", "message", "Invalid signature"));
            }

            // Use MoMo's orderId as momoOrderId
            String momoOrderId = momoParams.get("orderId");
            PaymentTransaction transaction = paymentTransactionRepository.findByMomoOrderId(momoOrderId)
                    .orElseThrow(
                            () -> new IllegalStateException("Transaction not found for momoOrderId: " + momoOrderId));

            Long orderId = transaction.getOrder().getId();
            String resultCode = momoParams.get("resultCode");

            // Chỉ cập nhật nếu trạng thái chưa được xử lý
            if (!transaction.getStatus().equals("SUCCESS") && !transaction.getStatus().equals("FAILED")) {
                if ("0".equals(resultCode)) {
                    transaction.setStatus("SUCCESS");
                    orderService.updateOrderStatus(orderId, "CONFIRMED");
                } else {
                    transaction.setStatus("FAILED");
                    orderService.updateOrderStatus(orderId, "CANCELLED");
                }
                transaction.setMoMoResponse(truncateString(momoParams.toString(), MAX_RESPONSE_LENGTH));
                paymentTransactionRepository.save(transaction);
            }

            return ResponseEntity.ok(Map.of(
                    "status", "0".equals(resultCode) ? "success" : "failed",
                    "message",
                    "0".equals(resultCode) ? "Payment successful" : "Payment failed: " + momoParams.get("message"),
                    "orderId", String.valueOf(orderId)));
        } catch (Exception e) {
            LOGGER.error("Error processing MoMo callback: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("status", "failed", "message", "Callback processing failed"));
        }
    }

    @PostMapping("/ipn")
    public ResponseEntity<Map<String, String>> handleMoMoIPN(@RequestBody Map<String, String> momoParams) {
        LOGGER.info("Received MoMo IPN: {}", momoParams);
        try {
            if (!validateCallbackSignature(momoParams)) {
                LOGGER.error("Invalid signature in MoMo IPN");
                return ResponseEntity.badRequest().body(Map.of("status", "failed", "message", "Invalid signature"));
            }

            // Use MoMo's orderId as momoOrderId
            String momoOrderId = momoParams.get("orderId");
            PaymentTransaction transaction = paymentTransactionRepository.findByMomoOrderId(momoOrderId)
                    .orElseThrow(
                            () -> new IllegalStateException("Transaction not found for momoOrderId: " + momoOrderId));

            Long orderId = transaction.getOrder().getId();
            String resultCode = momoParams.get("resultCode");

            // Chỉ cập nhật nếu trạng thái chưa được xử lý
            if (!transaction.getStatus().equals("SUCCESS") && !transaction.getStatus().equals("FAILED")) {
                if ("0".equals(resultCode)) {
                    transaction.setStatus("SUCCESS");
                    orderService.updateOrderStatus(orderId, "CONFIRMED");
                } else {
                    transaction.setStatus("FAILED");
                    orderService.updateOrderStatus(orderId, "CANCELLED");
                }
                transaction.setMoMoResponse(truncateString(momoParams.toString(), MAX_RESPONSE_LENGTH));
                paymentTransactionRepository.save(transaction);
            }

            return ResponseEntity.ok(Map.of("status", "success", "message", "IPN processed"));
        } catch (Exception e) {
            LOGGER.error("Error processing MoMo IPN: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("status", "failed", "message", "IPN processing failed"));
        }
    }

    @GetMapping("/check/{momoOrderId}")
    public ResponseEntity<SimpleResponse<Map<String, String>>> checkPaymentStatus(@PathVariable String momoOrderId) {
        try {
            LOGGER.info("Checking MoMo payment status for momoOrderId: {}", momoOrderId);
            PaymentTransaction transaction = paymentTransactionRepository.findByMomoOrderId(momoOrderId)
                    .orElseThrow(
                            () -> new IllegalStateException("Transaction not found for momoOrderId: " + momoOrderId));

            String response = queryPaymentStatus(momoOrderId);
            JSONObject responseJson = new JSONObject(response);
            String resultCode = responseJson.optString("resultCode", "-1");

            SimpleResponse<Map<String, String>> simpleResponse = new SimpleResponse<>();
            Map<String, String> result = new HashMap<>();
            Long orderId = transaction.getOrder().getId();

            if ("0".equals(resultCode)) {
                if (!transaction.getStatus().equals("SUCCESS")) {
                    transaction.setStatus("SUCCESS");
                    orderService.updateOrderStatus(orderId, "CONFIRMED");
                }
                result.put("status", "success");
                result.put("message", "Payment successful");
            } else {
                if (!transaction.getStatus().equals("FAILED")) {
                    transaction.setStatus("FAILED");
                    orderService.updateOrderStatus(orderId, "CANCELLED");
                }
                result.put("status", "failed");
                result.put("message", "Payment failed: " + responseJson.optString("message"));
            }
            result.put("orderId", String.valueOf(orderId));
            transaction.setMoMoResponse(truncateString(response, MAX_RESPONSE_LENGTH));
            paymentTransactionRepository.save(transaction);

            simpleResponse.setContent(result);
            simpleResponse.setMessage("Payment status checked");
            return ResponseEntity.ok(simpleResponse);
        } catch (Exception e) {
            LOGGER.error("Failed to check payment status: {}", e.getMessage(), e);
            SimpleResponse<Map<String, String>> response = new SimpleResponse<>();
            response.setMessage("Failed to check payment status: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    private void validateRequest(MoMoRequestDto request) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (request.getOrderId() == null || request.getOrderId().trim().isEmpty()) {
            throw new IllegalArgumentException("Order ID is required");
        }
    }

    private Map<String, String> buildMoMoParams(String requestId, String momoOrderId, String amount,
            String orderInfo) {
        Map<String, String> params = new HashMap<>();
        params.put("partnerCode", PaymentGatewayConstant.PARTNER_CODE);
        params.put("accessKey", PaymentGatewayConstant.ACCESS_KEY);
        params.put("requestId", requestId);
        params.put("amount", amount);
        params.put("orderId", momoOrderId);
        params.put("orderInfo", orderInfo);
        params.put("redirectUrl", PaymentGatewayConstant.REDIRECT_URL);
        params.put("ipnUrl", PaymentGatewayConstant.IPN_URL);
        params.put("extraData", "");
        params.put("requestType", PaymentGatewayConstant.REQUEST_TYPE);
        params.put("lang", "en");
        return params;
    }

    private String generateSignature(Map<String, String> params) throws Exception {
        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&ipnUrl=%s&orderId=%s&orderInfo=%s&partnerCode=%s&redirectUrl=%s&requestId=%s&requestType=%s",
                params.get("accessKey"), params.get("amount"), params.get("extraData"), params.get("ipnUrl"),
                params.get("orderId"), params.get("orderInfo"), params.get("partnerCode"), params.get("redirectUrl"),
                params.get("requestId"), params.get("requestType"));
        return signHmacSHA256(rawSignature, PaymentGatewayConstant.SECRET_KEY);
    }

    private boolean validateCallbackSignature(Map<String, String> params) throws Exception {
        String receivedSignature = params.get("signature");
        if (receivedSignature == null) {
            return false;
        }
        String rawSignature = String.format(
                "accessKey=%s&amount=%s&extraData=%s&message=%s&orderId=%s&orderInfo=%s&orderType=%s&partnerCode=%s&requestId=%s&responseTime=%s&resultCode=%s&transId=%s",
                PaymentGatewayConstant.ACCESS_KEY, params.getOrDefault("amount", ""),
                params.getOrDefault("extraData", ""), params.getOrDefault("message", ""),
                params.getOrDefault("orderId", ""), params.getOrDefault("orderInfo", ""),
                params.getOrDefault("orderType", ""), PaymentGatewayConstant.PARTNER_CODE,
                params.getOrDefault("requestId", ""), params.getOrDefault("responseTime", ""),
                params.getOrDefault("resultCode", ""), params.getOrDefault("transId", ""));
        String calculatedSignature = signHmacSHA256(rawSignature, PaymentGatewayConstant.SECRET_KEY);
        return calculatedSignature.equals(receivedSignature);
    }

    private String sendPaymentRequest(Map<String, String> params) throws Exception {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(PaymentGatewayConstant.PAYMENT_API_URL);
            httpPost.setHeader("Content-Type", "application/json");
            JSONObject requestBody = new JSONObject(params);
            httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            }
        }
    }

    private String queryPaymentStatus(String momoOrderId) throws Exception {
        String requestId = PaymentGatewayConstant.PARTNER_CODE + System.currentTimeMillis();
        String rawSignature = String.format(
                "accessKey=%s&orderId=%s&partnerCode=%s&requestId=%s",
                PaymentGatewayConstant.ACCESS_KEY, momoOrderId, PaymentGatewayConstant.PARTNER_CODE, requestId);
        String signature = signHmacSHA256(rawSignature, PaymentGatewayConstant.SECRET_KEY);

        JSONObject requestBody = new JSONObject();
        requestBody.put("partnerCode", PaymentGatewayConstant.PARTNER_CODE);
        requestBody.put("accessKey", PaymentGatewayConstant.ACCESS_KEY);
        requestBody.put("requestId", requestId);
        requestBody.put("orderId", momoOrderId);
        requestBody.put("signature", signature);
        requestBody.put("lang", "en");

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(PaymentGatewayConstant.QUERY_API_URL);
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setEntity(new StringEntity(requestBody.toString(), StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            }
        }
    }

    private String signHmacSHA256(String data, String key) throws Exception {
        Mac hmacSHA256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmacSHA256.init(secretKey);
        byte[] hash = hmacSHA256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private String truncateString(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) {
            return input;
        }
        LOGGER.warn("Truncating MoMo response to {} characters to avoid data truncation", maxLength);
        return input.substring(0, maxLength);
    }
}