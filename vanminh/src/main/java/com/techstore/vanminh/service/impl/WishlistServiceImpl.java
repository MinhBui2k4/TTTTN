package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.WishlistItemDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.entity.Product;
import com.techstore.vanminh.entity.User;
import com.techstore.vanminh.entity.Wishlist;
import com.techstore.vanminh.entity.WishlistItem;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.ProductRepository;
import com.techstore.vanminh.repository.UserRepository;
import com.techstore.vanminh.repository.WishlistItemRepository;
import com.techstore.vanminh.repository.WishlistRepository;
import com.techstore.vanminh.service.WishlistService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));
    }

    @Override
    public BaseResponse<WishlistItemDTO> addItemToWishlist(WishlistItemDTO wishlistItemDTO) {
        User user = getCurrentUser();

        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUser(user);
                    newWishlist.setItems(new ArrayList<>());
                    return wishlistRepository.save(newWishlist);
                });

        Product product = productRepository.findById(wishlistItemDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sản phẩm không tìm thấy với id: " + wishlistItemDTO.getProductId()));

        // Kiểm tra sản phẩm đã có trong danh sách chưa
        Optional<WishlistItem> existingItem = wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getId(),
                product.getId());
        if (existingItem.isPresent()) {
            throw new BadRequestException("Sản phẩm đã có trong danh sách yêu thích");
        }

        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setWishlist(wishlist);
        wishlistItem.setProduct(product);
        wishlistItemRepository.save(wishlistItem);
        wishlist.getItems().add(wishlistItem);

        BaseResponse<WishlistItemDTO> response = new BaseResponse<>();
        response.setContent(wishlist.getItems().stream()
                .map(item -> {
                    WishlistItemDTO itemDTO = modelMapper.map(item, WishlistItemDTO.class);
                    itemDTO.setProductName(item.getProduct().getName());
                    itemDTO.setProductPrice(item.getProduct().getPrice());
                    itemDTO.setAvailability(item.getProduct().isAvailability());
                    return itemDTO;
                })
                .collect(Collectors.toList()));
        response.setMessage("Thêm sản phẩm vào danh sách yêu thích thành công.");
        return response;
    }

    @Override
    public BaseResponse<WishlistItemDTO> getWishlist() {
        User user = getCurrentUser();

        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUser(user);
                    newWishlist.setItems(new ArrayList<>());
                    return wishlistRepository.save(newWishlist);
                });

        BaseResponse<WishlistItemDTO> response = new BaseResponse<>();
        response.setContent(wishlist.getItems().stream()
                .map(item -> {
                    WishlistItemDTO itemDTO = modelMapper.map(item, WishlistItemDTO.class);
                    itemDTO.setProductName(item.getProduct().getName());
                    itemDTO.setProductPrice(item.getProduct().getPrice());
                    itemDTO.setAvailability(item.getProduct().isAvailability());
                    return itemDTO;
                })
                .collect(Collectors.toList()));
        return response;
    }

    @Override
    public BaseResponse<WishlistItemDTO> removeItemFromWishlist(Long productId) {
        User user = getCurrentUser();

        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh sách yêu thích không tìm thấy"));

        WishlistItem wishlistItem = wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không có trong danh sách yêu thích"));

        wishlist.getItems().remove(wishlistItem);
        wishlistItemRepository.delete(wishlistItem);

        BaseResponse<WishlistItemDTO> response = new BaseResponse<>();
        response.setContent(wishlist.getItems().stream()
                .map(item -> {
                    WishlistItemDTO itemDTO = modelMapper.map(item, WishlistItemDTO.class);
                    itemDTO.setProductName(item.getProduct().getName());
                    itemDTO.setProductPrice(item.getProduct().getPrice());
                    itemDTO.setAvailability(item.getProduct().isAvailability());
                    return itemDTO;
                })
                .collect(Collectors.toList()));
        response.setMessage("Xóa sản phẩm khỏi danh sách yêu thích thành công.");
        return response;
    }

    @Override
    public void clearWishlist() {
        User user = getCurrentUser();

        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh sách yêu thích không tìm thấy"));

        wishlistItemRepository.deleteAll(wishlist.getItems());
        wishlist.setItems(new ArrayList<>());
        wishlistRepository.save(wishlist);
    }
}