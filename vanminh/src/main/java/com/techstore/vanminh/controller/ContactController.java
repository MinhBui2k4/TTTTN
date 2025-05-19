package com.techstore.vanminh.controller;

import com.techstore.vanminh.dto.ContactDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.service.ContactService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {

    @Autowired
    private ContactService contactService;

    @PostMapping
    public ResponseEntity<ContactDTO> createContact(@Valid @RequestBody ContactDTO contactDTO) {
        return ResponseEntity.ok(contactService.createContact(contactDTO));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<BaseResponse<ContactDTO>> getAllContacts(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        return ResponseEntity.ok(contactService.getAllContacts(pageNumber, pageSize, sortBy, sortOrder));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ContactDTO> getContact(@PathVariable Long id) {
        return ResponseEntity.ok(contactService.getContactById(id));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<ContactDTO> updateContactStatus(@PathVariable Long id,
            @RequestBody StatusUpdateRequest request) {
        return ResponseEntity.ok(contactService.updateContactStatus(id, request.getStatus()));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteContact(@PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }

    // DTO cho yêu cầu cập nhật trạng thái
    public static class StatusUpdateRequest {
        private String status;

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}