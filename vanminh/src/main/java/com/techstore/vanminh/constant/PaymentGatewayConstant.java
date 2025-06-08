package com.techstore.vanminh.constant;

public class PaymentGatewayConstant {
    public static final String PARTNER_CODE = "MOMO";
    public static final String ACCESS_KEY = "F8BBA842ECF85";
    public static final String SECRET_KEY = "K951B6PE1waDMi640xX08PD3vg6EkVlz";
    public static final String REDIRECT_URL = "http://localhost:3000/checkout/success"; // Update to frontend success
                                                                                        // page
    public static final String IPN_URL = "http://localhost:8080/api/payment/momo/ipn"; // Server-to-server notification
    public static final String REQUEST_TYPE = "payWithMethod";
    public static final String PAYMENT_API_URL = "https://test-payment.momo.vn/v2/gateway/api/create";
    public static final String QUERY_API_URL = "https://test-payment.momo.vn/v2/gateway/api/query";
}