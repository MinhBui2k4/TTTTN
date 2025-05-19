package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.PaymentMethodDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.entity.PaymentMethod;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.OrderRepository;
import com.techstore.vanminh.repository.PaymentMethodRepository;
import com.techstore.vanminh.service.PaymentMethodService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentMethodServiceImpl implements PaymentMethodService {

    @Autowired
    private PaymentMethodRepository paymentMethodRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public PaymentMethodDTO createPaymentMethod(PaymentMethodDTO paymentMethodDTO) {
        if (paymentMethodRepository.findByName(paymentMethodDTO.getName()).isPresent()) {
            throw new BadRequestException("Phương thức thanh toán đã tồn tại: " + paymentMethodDTO.getName());
        }
        PaymentMethod paymentMethod = modelMapper.map(paymentMethodDTO, PaymentMethod.class);
        paymentMethod = paymentMethodRepository.save(paymentMethod);
        return modelMapper.map(paymentMethod, PaymentMethodDTO.class);
    }

    @Override
    public BaseResponse<PaymentMethodDTO> getAllPaymentMethods(int pageNumber, int pageSize, String sortBy,
            String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<PaymentMethod> paymentMethods = paymentMethodRepository.findAll(pageable);

        BaseResponse<PaymentMethodDTO> response = new BaseResponse<>();
        response.setContent(paymentMethods.getContent().stream()
                .map(paymentMethod -> modelMapper.map(paymentMethod, PaymentMethodDTO.class))
                .collect(Collectors.toList()));
        response.setPageNumber(paymentMethods.getNumber());
        response.setPageSize(paymentMethods.getSize());
        response.setTotalElements(paymentMethods.getTotalElements());
        response.setTotalPages(paymentMethods.getTotalPages());
        response.setLastPage(paymentMethods.isLast());
        return response;
    }

    @Override
    public PaymentMethodDTO getPaymentMethodById(Long id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Phương thức thanh toán không tìm thấy với id: " + id));
        return modelMapper.map(paymentMethod, PaymentMethodDTO.class);
    }

    @Override
    public PaymentMethodDTO updatePaymentMethod(Long id, PaymentMethodDTO paymentMethodDTO) {
        PaymentMethod existingPaymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Phương thức thanh toán không tìm thấy với id: " + id));
        if (!existingPaymentMethod.getName().equals(paymentMethodDTO.getName()) &&
                paymentMethodRepository.findByName(paymentMethodDTO.getName()).isPresent()) {
            throw new BadRequestException("Phương thức thanh toán đã tồn tại: " + paymentMethodDTO.getName());
        }
        modelMapper.map(paymentMethodDTO, existingPaymentMethod);
        existingPaymentMethod = paymentMethodRepository.save(existingPaymentMethod);
        return modelMapper.map(existingPaymentMethod, PaymentMethodDTO.class);
    }

    @Override
    public void deletePaymentMethod(Long id) {
        PaymentMethod paymentMethod = paymentMethodRepository.findById(id)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Phương thức thanh toán không tìm thấy với id: " + id));

        // Kiểm tra xem phương thức có đang được sử dụng trong đơn hàng không
        long orderCount = orderRepository.countByPaymentMethodId(id);
        if (orderCount > 0) {
            throw new BadRequestException("Không thể xóa phương thức thanh toán đang được sử dụng trong đơn hàng");
        }

        paymentMethodRepository.delete(paymentMethod);
    }
}