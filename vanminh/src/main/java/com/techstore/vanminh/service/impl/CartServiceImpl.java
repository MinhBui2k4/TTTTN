package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.CartDTO;
import com.techstore.vanminh.dto.CartItemDTO;
import com.techstore.vanminh.entity.Cart;
import com.techstore.vanminh.entity.CartItem;
import com.techstore.vanminh.entity.Product;
import com.techstore.vanminh.entity.User;
import com.techstore.vanminh.exception.BadRequestException;
import com.techstore.vanminh.exception.ResourceNotFoundException;
import com.techstore.vanminh.repository.CartItemRepository;
import com.techstore.vanminh.repository.CartRepository;
import com.techstore.vanminh.repository.ProductRepository;
import com.techstore.vanminh.repository.UserRepository;
import com.techstore.vanminh.service.CartService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CartServiceImpl implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public CartDTO getCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        return modelMapper.map(cart, CartDTO.class);
    }

    // @Override
    // public BaseResponse<CartItemDTO> getCart(int pageNumber, int pageSize, String
    // sortBy, String sortOrder) {
    // String email =
    // SecurityContextHolder.getContext().getAuthentication().getName();
    // User user = userRepository.findByEmail(email)
    // .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm
    // thấy"));

    // Cart cart = cartRepository.findByUserId(user.getId())
    // .orElseGet(() -> {
    // Cart newCart = new Cart();
    // newCart.setUser(user);
    // return cartRepository.save(newCart);
    // });

    // List<CartItem> allItems = new ArrayList<>(cart.getItems());

    // // Sắp xếp
    // Comparator<CartItem> comparator;
    // switch (sortBy.toLowerCase()) {
    // case "quantity" -> comparator = Comparator.comparing(CartItem::getQuantity);
    // case "productname" ->
    // comparator = Comparator.comparing(item -> item.getProduct().getName(),
    // String.CASE_INSENSITIVE_ORDER);
    // default -> comparator = Comparator.comparing(CartItem::getId);
    // }

    // if("desc".equalsIgnoreCase(sortOrder))

    // {
    // comparator = comparator.reversed();
    // }

    // allItems.sort(comparator);

    // // Tạo Page-like phân trang thủ công
    // int totalElements = allItems.size();
    // int start = Math.min(pageNumber * pageSize, totalElements);
    // int end = Math.min(start + pageSize, totalElements);
    // List<CartItem> paginatedItems = allItems.subList(start, end);

    // // Convert to DTO
    // List<CartItemDTO> cartItemDTOs = paginatedItems.stream()
    // .map(item -> modelMapper.map(item, CartItemDTO.class))
    // .collect(Collectors.toList());

    // // Build BaseResponse như mẫu Category
    // BaseResponse<CartItemDTO> response = new
    // BaseResponse<>();response.setContent(cartItemDTOs);response.setPageNumber(pageNumber);response.setPageSize(pageSize);response.setTotalElements((long)totalElements);response.setTotalPages((int)Math.ceil((double)totalElements/pageSize));response.setLastPage(end>=totalElements);

    // return response;
    // }

    @Override
    public CartDTO addItemToCart(CartItemDTO cartItemDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sản phẩm không tìm thấy với id: " + cartItemDTO.getProductId()));

        if (cartItemDTO.getQuantity() <= 0 || cartItemDTO.getQuantity() > product.getQuantity()) {
            throw new BadRequestException("Số lượng không hợp lệ");
        }

        CartItem cartItem = new CartItem();
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(cartItemDTO.getQuantity());

        cartItemRepository.save(cartItem);

        return modelMapper.map(cart, CartDTO.class);
    }

    @Override
    public CartDTO updateCartItem(Long itemId, CartItemDTO cartItemDTO) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục giỏ hàng không tìm thấy với id: " + itemId));

        Product product = productRepository.findById(cartItem.getProduct().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Sản phẩm không tìm thấy"));

        if (cartItemDTO.getQuantity() <= 0 || cartItemDTO.getQuantity() > product.getQuantity()) {
            throw new BadRequestException("Số lượng không hợp lệ");
        }

        cartItem.setQuantity(cartItemDTO.getQuantity());
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        return modelMapper.map(cart, CartDTO.class);
    }

    @Override
    public CartDTO removeItemFromCart(Long itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục giỏ hàng không tìm thấy với id: " + itemId));

        Cart cart = cartItem.getCart();
        cartItemRepository.delete(cartItem);

        return modelMapper.map(cart, CartDTO.class);
    }

    @Override
    public void clearCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tìm thấy"));

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}