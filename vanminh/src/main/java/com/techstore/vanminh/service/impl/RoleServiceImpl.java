package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.RoleDTO;
import com.techstore.vanminh.entity.Role;
import com.techstore.vanminh.repository.RoleRepository;
import com.techstore.vanminh.service.RoleService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoleDTO getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        return convertToDTO(role);
    }

    @Override
    public RoleDTO saveRole(RoleDTO roleDTO) {
        Role role = convertToEntity(roleDTO);
        Role savedRole = roleRepository.save(role);
        return convertToDTO(savedRole);
    }

    @Override
    public RoleDTO updateRole(Long id, RoleDTO updatedRoleDTO) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Chuyển từ String -> Enum
        try {
            Role.RoleName roleName = Role.RoleName.valueOf(updatedRoleDTO.getName().toUpperCase());
            existingRole.setName(roleName);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role name: " + updatedRoleDTO.getName());
        }

        return convertToDTO(roleRepository.save(existingRole));
    }

    @Override
    public void deleteRole(Long id) {
        roleRepository.deleteById(id);
    }

    private RoleDTO convertToDTO(Role role) {
        RoleDTO dto = new RoleDTO();
        dto.setId(role.getId());
        dto.setName(role.getName().name()); // Enum -> String
        return dto;
    }

    private Role convertToEntity(RoleDTO dto) {
        Role role = new Role();
        role.setId(dto.getId());

        try {
            role.setName(Role.RoleName.valueOf(dto.getName().toUpperCase())); // String -> Enum
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role name: " + dto.getName());
        }

        return role;
    }

}
