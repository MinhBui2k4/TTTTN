package com.techstore.vanminh.controller;

import com.techstore.vanminh.dto.AddressDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/addresses")
@PreAuthorize("hasRole('ROLE_USER')")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @PostMapping
    public ResponseEntity<AddressDTO> createAddress(@Valid @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(addressService.createAddress(addressDTO));
    }

    @GetMapping
    public ResponseEntity<BaseResponse<AddressDTO>> getUserAddresses(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {
        return ResponseEntity.ok(addressService.getUserAddresses(pageNumber, pageSize, sortBy, sortOrder));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressDTO> getAddress(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.getAddressById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressDTO> updateAddress(@PathVariable Long id, @Valid @RequestBody AddressDTO addressDTO) {
        return ResponseEntity.ok(addressService.updateAddress(id, addressDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id) {
        addressService.deleteAddress(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/default")
    public ResponseEntity<AddressDTO> setDefaultAddress(@PathVariable Long id) {
        return ResponseEntity.ok(addressService.setDefaultAddress(id));
    }
}