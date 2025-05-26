package com.techstore.vanminh;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.techstore.vanminh.dto.AddressDTO;

import com.techstore.vanminh.dto.RoleDTO;
import com.techstore.vanminh.dto.WishlistItemDTO;
import com.techstore.vanminh.entity.Address;

import com.techstore.vanminh.entity.Role;
import com.techstore.vanminh.entity.WishlistItem;

import java.util.logging.Logger;

@SpringBootApplication
public class VanminhApplication {

	private static final Logger logger = Logger.getLogger(VanminhApplication.class.getName());

	public static void main(String[] args) {
		SpringApplication.run(VanminhApplication.class, args);
	}

	@Bean
	public ModelMapper modelMapper() {
		try {
			ModelMapper modelMapper = new ModelMapper();
			modelMapper.getConfiguration()
					.setMatchingStrategy(MatchingStrategies.STRICT)
					.setSkipNullEnabled(true);

			// Tạo typeMap cho Address -> AddressDTO
			TypeMap<Address, AddressDTO> addressTypeMap = modelMapper.createTypeMap(Address.class, AddressDTO.class);
			addressTypeMap.addMappings(mapper -> {
				logger.info("Configuring Address to AddressDTO mapping");
				mapper.map(Address::getId, AddressDTO::setId);
				mapper.map(Address::getName, AddressDTO::setName);
				mapper.map(Address::getPhone, AddressDTO::setPhone);
				mapper.map(Address::getAddress, AddressDTO::setAddress);
				mapper.map(Address::getWard, AddressDTO::setWard);
				mapper.map(Address::getDistrict, AddressDTO::setDistrict);
				mapper.map(Address::getProvince, AddressDTO::setProvince);
				mapper.map(Address::getType, AddressDTO::setType);
				mapper.map(Address::isDefault, AddressDTO::setDefault);
			});

			// Tạo typeMap cho Role -> RoleDTO
			TypeMap<Role, RoleDTO> roleTypeMap = modelMapper.createTypeMap(Role.class, RoleDTO.class);
			roleTypeMap.addMappings(mapper -> {
				logger.info("Configuring Role to RoleDTO mapping");
				mapper.map(Role::getId, RoleDTO::setId);
				mapper.map(src -> src.getName() != null ? src.getName().name() : null, RoleDTO::setName);
			});

			// TypeMap cho WishlistItem -> WishlistItemDTO
			TypeMap<WishlistItem, WishlistItemDTO> wishlistItemTypeMap = modelMapper.createTypeMap(WishlistItem.class,
					WishlistItemDTO.class);
			wishlistItemTypeMap.addMappings(mapper -> {
				logger.info("Configuring WishlistItem to WishlistItemDTO mapping");
				mapper.map(WishlistItem::getId, WishlistItemDTO::setId);
				mapper.map(src -> src.getWishlist() != null ? src.getWishlist().getId() : null,
						WishlistItemDTO::setWishlistId);
				mapper.map(src -> src.getProduct() != null ? src.getProduct().getId() : null,
						WishlistItemDTO::setProductId);
			});

			logger.info("ModelMapper configuration completed successfully");
			return modelMapper;
		} catch (Exception e) {
			logger.severe("Failed to configure ModelMapper: " + e.getMessage());
			throw new RuntimeException("Failed to configure ModelMapper", e);
		}
	}
}