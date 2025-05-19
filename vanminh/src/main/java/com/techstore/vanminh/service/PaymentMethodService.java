package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.PaymentMethodDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import org.springframework.data.domain.Pageable;

public interface PaymentMethodService {
    PaymentMethodDTO createPaymentMethod(PaymentMethodDTO paymentMethodDTO);

    BaseResponse<PaymentMethodDTO> getAllPaymentMethods(int pageNumber, int pageSize, String sortBy, String sortOrder);

    PaymentMethodDTO getPaymentMethodById(Long id);

    PaymentMethodDTO updatePaymentMethod(Long id, PaymentMethodDTO paymentMethodDTO);

    void deletePaymentMethod(Long id);
}