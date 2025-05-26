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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private static final Logger logger = Logger.getLogger(WishlistServiceImpl.class.getName());

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
        logger.info("Finding user with email: " + email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));
    }

    @Override
    public BaseResponse<WishlistItemDTO> addItemToWishlist(WishlistItemDTO wishlistItemDTO) {
        logger.info("Adding item to wishlist with productId: " + wishlistItemDTO.getProductId());
        User user = getCurrentUser();

        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUser(user);
                    newWishlist.setItems(new ArrayList<>());
                    return wishlistRepository.save(newWishlist);
                });
        logger.info("Found or created wishlist with id: " + wishlist.getId());

        Product product = productRepository.findById(wishlistItemDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sản phẩm không tìm thấy với id: " + wishlistItemDTO.getProductId()));
        logger.info("Found product with id: " + product.getId());

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
        logger.info("Saved wishlist item with id: " + wishlistItem.getId());

        BaseResponse<WishlistItemDTO> response = new BaseResponse<>();
        response.setContent(wishlist.getItems().stream()
                .map(this::mapToWishlistItemDTO)
                .collect(Collectors.toList()));
        response.setMessage("Thêm sản phẩm vào danh sách yêu thích thành công.");
        return response;
    }

    @Override
    public BaseResponse<WishlistItemDTO> getWishlist(int pageNumber, int pageSize) {
        logger.info("Getting wishlist for pageNumber: " + pageNumber + ", pageSize: " + pageSize);
        User user = getCurrentUser();

        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Wishlist newWishlist = new Wishlist();
                    newWishlist.setUser(user);
                    newWishlist.setItems(new ArrayList<>());
                    return wishlistRepository.save(newWishlist);
                });
        logger.info("Found wishlist with id: " + wishlist.getId());

        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<WishlistItem> itemPage = wishlistItemRepository.findByWishlistId(wishlist.getId(), pageable);
        logger.info("Found " + itemPage.getTotalElements() + " wishlist items");

        List<WishlistItemDTO> pageContent = itemPage.getContent().stream()
                .map(this::mapToWishlistItemDTO)
                .collect(Collectors.toList());

        BaseResponse<WishlistItemDTO> response = new BaseResponse<>();
        response.setContent(pageContent);
        response.setPageNumber(itemPage.getNumber());
        response.setPageSize(itemPage.getSize());
        response.setTotalElements(itemPage.getTotalElements());
        response.setTotalPages(itemPage.getTotalPages());
        response.setLastPage(itemPage.isLast());
        logger.info("Completed getWishlist");
        return response;
    }

    @Override
    public BaseResponse<WishlistItemDTO> removeItemFromWishlist(Long productId) {
        logger.info("Removing item with productId: " + productId + " from wishlist");
        User user = getCurrentUser();

        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh sách yêu thích không tìm thấy"));
        logger.info("Found wishlist with id: " + wishlist.getId());

        WishlistItem wishlistItem = wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getId(), productId)
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không có trong danh sách yêu thích"));
        logger.info("Found wishlist item with id: " + wishlistItem.getId());

        wishlist.getItems().remove(wishlistItem);
        wishlistItemRepository.delete(wishlistItem);

        BaseResponse<WishlistItemDTO> response = new BaseResponse<>();
        response.setContent(wishlist.getItems().stream()
                .map(this::mapToWishlistItemDTO)
                .collect(Collectors.toList()));
        response.setMessage("Xóa sản phẩm khỏi danh sách yêu thích thành công.");
        return response;
    }

    @Override
    @Transactional
    public void clearWishlist() {
        logger.info("Clearing wishlist for current user");
        User user = getCurrentUser();

        Wishlist wishlist = wishlistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Danh sách yêu thích không tìm thấy"));
        logger.info("Found wishlist with id: " + wishlist.getId());

        wishlistItemRepository.deleteAll(wishlist.getItems());
        wishlist.getItems().clear();
        wishlistRepository.save(wishlist);
        logger.info("Cleared wishlist");
    }

    private WishlistItemDTO mapToWishlistItemDTO(WishlistItem item) {
        logger.info("Mapping WishlistItem to WishlistItemDTO: " + item.getId());
        WishlistItemDTO itemDTO = new WishlistItemDTO();
        itemDTO.setId(item.getId());
        itemDTO.setWishlistId(item.getWishlist() != null ? item.getWishlist().getId() : null);
        itemDTO.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
        itemDTO.setProductName(item.getProduct() != null ? item.getProduct().getName() : null);
        itemDTO.setProductPrice(item.getProduct() != null ? item.getProduct().getPrice() : null);
        itemDTO.setAvailability(item.getProduct() != null ? item.getProduct().isAvailability() : false);
        logger.info("Completed mapping WishlistItem to WishlistItemDTO: " + item.getId());
        return itemDTO;
    }
}