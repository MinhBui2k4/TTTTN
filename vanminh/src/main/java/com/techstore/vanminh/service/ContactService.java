package com.techstore.vanminh.service;

import com.techstore.vanminh.dto.ContactDTO;
import com.techstore.vanminh.dto.response.BaseResponse;

public interface ContactService {
    ContactDTO createContact(ContactDTO contactDTO);

    BaseResponse<ContactDTO> getAllContacts(int pageNumber, int pageSize, String sortBy, String sortOrder);

    ContactDTO getContactById(Long id);

    ContactDTO updateContactStatus(Long id, String status);

    void deleteContact(Long id);
}