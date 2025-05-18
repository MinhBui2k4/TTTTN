package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.AddressDTO;
import com.techstore.vanminh.dto.response.BaseResponse;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO);

    BaseResponse<AddressDTO> getUserAddresses(int pageNumber, int pageSize, String sortBy, String sortOrder);

    AddressDTO getAddressById(Long id);

    AddressDTO updateAddress(Long id, AddressDTO addressDTO);

    void deleteAddress(Long id);

    AddressDTO setDefaultAddress(Long id);
}