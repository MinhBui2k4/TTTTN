package com.techstore.vanminh.controller;

import com.techstore.vanminh.dto.PaymentMethodDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.service.PaymentMethodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/payment-methods")
public class PaymentMethodController {

    @Autowired
    private PaymentMethodService paymentMethodService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<PaymentMethodDTO> createPaymentMethod(@Valid @RequestBody PaymentMethodDTO paymentMethodDTO) {
        return ResponseEntity.ok(paymentMethodService.createPaymentMethod(paymentMethodDTO));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<PaymentMethodDTO>> getAllPaymentMethods(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        return ResponseEntity.ok(paymentMethodService.getAllPaymentMethods(pageNumber, pageSize, sortBy, sortOrder));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodDTO> getPaymentMethod(@PathVariable Long id) {
        return ResponseEntity.ok(paymentMethodService.getPaymentMethodById(id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethodDTO> updatePaymentMethod(@PathVariable Long id,
            @Valid @RequestBody PaymentMethodDTO paymentMethodDTO) {
        return ResponseEntity.ok(paymentMethodService.updatePaymentMethod(id, paymentMethodDTO));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePaymentMethod(@PathVariable Long id) {
        paymentMethodService.deletePaymentMethod(id);
        return ResponseEntity.ok("Xóa phương thức thanh toán thành công");
    }

}