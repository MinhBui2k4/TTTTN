package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.ContactDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.entity.Contact;
import com.techstore.vanminh.entity.User;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.ContactRepository;
import com.techstore.vanminh.repository.UserRepository;
import com.techstore.vanminh.service.ContactService;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@Transactional
public class ContactServiceImpl implements ContactService {

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public ContactDTO createContact(ContactDTO contactDTO) {
        Contact contact = modelMapper.map(contactDTO, Contact.class);

        // Nếu người dùng đăng nhập, gắn user
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (email != null && !email.equals("anonymousUser")) {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));
            contact.setUser(user);
        }

        contact = contactRepository.save(contact);
        return modelMapper.map(contact, ContactDTO.class);
    }

    @Override
    public BaseResponse<ContactDTO> getAllContacts(int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Sort sort = sortOrder.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<Contact> contacts = contactRepository.findAll(pageable);

        BaseResponse<ContactDTO> response = new BaseResponse<>();
        response.setContent(contacts.getContent().stream()
                .map(contact -> modelMapper.map(contact, ContactDTO.class))
                .collect(Collectors.toList()));
        response.setPageNumber(contacts.getNumber());
        response.setPageSize(contacts.getSize());
        response.setTotalElements(contacts.getTotalElements());
        response.setTotalPages(contacts.getTotalPages());
        response.setLastPage(contacts.isLast());
        return response;
    }

    @Override
    public ContactDTO getContactById(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thông tin liên hệ không tìm thấy với id: " + id));
        return modelMapper.map(contact, ContactDTO.class);
    }

    @Override
    public ContactDTO updateContactStatus(Long id, String status) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thông tin liên hệ không tìm thấy với id: " + id));

        try {
            Contact.ContactStatus newStatus = Contact.ContactStatus.valueOf(status.toUpperCase());
            contact.setStatus(newStatus);
            contact = contactRepository.save(contact);
            return modelMapper.map(contact, ContactDTO.class);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Trạng thái không hợp lệ: " + status);
        }
    }

    @Override
    public void deleteContact(Long id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Thông tin liên hệ không tìm thấy với id: " + id));
        contactRepository.delete(contact);
    }
}