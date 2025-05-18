package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.AddressDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.entity.Address;
import com.techstore.vanminh.entity.User;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.AddressRepository;
import com.techstore.vanminh.repository.OrderRepository;
import com.techstore.vanminh.repository.UserRepository;
import com.techstore.vanminh.service.AddressService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Sort;

import java.util.stream.Collectors;

@Service
@Transactional
public class AddressServiceImpl implements AddressService {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ModelMapper modelMapper;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));
    }

    @Override
    public AddressDTO createAddress(AddressDTO addressDTO) {
        User user = getCurrentUser();

        Address address = modelMapper.map(addressDTO, Address.class);
        address.setUser(user);

        // Nếu là địa chỉ mặc định, xóa isDefault của các địa chỉ khác
        if (address.isDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(existingDefault -> {
                        existingDefault.setDefault(false);
                        addressRepository.save(existingDefault);
                    });
        }

        address = addressRepository.save(address);
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public BaseResponse<AddressDTO> getUserAddresses(int pageNumber, int pageSize, String sortBy, String sortOrder) {
        User user = getCurrentUser();

        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<Address> addresses = addressRepository.findByUserId(user.getId(), pageable);

        BaseResponse<AddressDTO> response = new BaseResponse<>();
        response.setContent(addresses.getContent().stream()
                .map(address -> modelMapper.map(address, AddressDTO.class))
                .collect(Collectors.toList()));
        response.setPageNumber(addresses.getNumber());
        response.setPageSize(addresses.getSize());
        response.setTotalElements(addresses.getTotalElements());
        response.setTotalPages(addresses.getTotalPages());
        response.setLastPage(addresses.isLast());

        return response;
    }

    @Override
    public AddressDTO getAddressById(Long id) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ không tìm thấy hoặc không thuộc về bạn"));
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public AddressDTO updateAddress(Long id, AddressDTO addressDTO) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ không tìm thấy hoặc không thuộc về bạn"));

        modelMapper.map(addressDTO, address);

        // Nếu cập nhật thành địa chỉ mặc định, xóa isDefault của các địa chỉ khác
        if (address.isDefault()) {
            addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                    .ifPresent(existingDefault -> {
                        if (!existingDefault.getId().equals(id)) {
                            existingDefault.setDefault(false);
                            addressRepository.save(existingDefault);
                        }
                    });
        }

        address = addressRepository.save(address);
        return modelMapper.map(address, AddressDTO.class);
    }

    @Override
    public void deleteAddress(Long id) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ không tìm thấy hoặc không thuộc về bạn"));

        // Kiểm tra xem địa chỉ có đang được sử dụng trong đơn hàng không
        long orderCount = orderRepository.countByShippingAddressId(id);
        if (orderCount > 0) {
            throw new BadRequestException("Không thể xóa địa chỉ đang được sử dụng trong đơn hàng");
        }

        addressRepository.delete(address);
    }

    @Override
    public AddressDTO setDefaultAddress(Long id) {
        User user = getCurrentUser();
        Address address = addressRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Địa chỉ không tìm thấy hoặc không thuộc về bạn"));

        // Xóa isDefault của các địa chỉ khác
        addressRepository.findByUserIdAndIsDefaultTrue(user.getId())
                .ifPresent(existingDefault -> {
                    if (!existingDefault.getId().equals(id)) {
                        existingDefault.setDefault(false);
                        addressRepository.save(existingDefault);
                    }
                });

        address.setDefault(true);
        address = addressRepository.save(address);
        return modelMapper.map(address, AddressDTO.class);
    }
}