package com.techstore.vanminh;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.techstore.vanminh.dto.AddressDTO;
import com.techstore.vanminh.dto.NewsDTO;
import com.techstore.vanminh.dto.RoleDTO;
import com.techstore.vanminh.dto.WishlistDTO;
import com.techstore.vanminh.dto.WishlistItemDTO;
import com.techstore.vanminh.entity.Address;
import com.techstore.vanminh.entity.News;
import com.techstore.vanminh.entity.Role;
import com.techstore.vanminh.entity.Wishlist;
import com.techstore.vanminh.entity.WishlistItem;

import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.stream.Collectors;

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
				mapper.map(src -> src.getUser().getId(), AddressDTO::setUserId); // Add this line for userId mapping
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

			// Mapping for Wishlist to WishlistDTO
			TypeMap<Wishlist, WishlistDTO> wishlistTypeMap = modelMapper.createTypeMap(Wishlist.class,
					WishlistDTO.class);
			wishlistTypeMap.addMappings(mapper -> {
				logger.info("Configuring Wishlist to WishlistDTO mapping");
				mapper.map(Wishlist::getId, WishlistDTO::setId);
				mapper.map(src -> src.getUser() != null ? src.getUser().getId() : null, WishlistDTO::setUserId);
				mapper.map(src -> src.getItems() != null ? src.getItems().stream()
						.map(item -> modelMapper.map(item, WishlistItemDTO.class))
						.collect(Collectors.toList()) : new ArrayList<>(), WishlistDTO::setItems);
			});

			// Add mapping for News to NewsDTO
			TypeMap<News, NewsDTO> newsTypeMap = modelMapper.createTypeMap(News.class, NewsDTO.class);
			newsTypeMap.addMappings(mapper -> {
				logger.info("Configuring News to NewsDTO mapping");
				mapper.map(News::getId, NewsDTO::setId);
				mapper.map(News::getTitle, NewsDTO::setTitle);
				mapper.map(News::getContent, NewsDTO::setContent);
				mapper.map(News::getImage, NewsDTO::setImage);
				mapper.map(News::getCreatedAt, NewsDTO::setCreatedAt);
				mapper.map(News::getUpdatedAt, NewsDTO::setUpdatedAt);
			});

			logger.info("ModelMapper configuration completed successfully");
			return modelMapper;
		} catch (Exception e) {
			logger.severe("Failed to configure ModelMapper: " + e.getMessage());
			throw new RuntimeException("Failed to configure ModelMapper", e);
		}
	}
}