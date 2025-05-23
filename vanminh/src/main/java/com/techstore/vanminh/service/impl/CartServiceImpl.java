package com.techstore.vanminh.service.impl;

import com.techstore.vanminh.dto.CartDTO;
import com.techstore.vanminh.dto.CartItemDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
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
import com.techstore.vanminh.util.CartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private CartMapper cartMapper;

    @Override
    public BaseResponse<CartDTO> getCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        BaseResponse<CartDTO> response = new BaseResponse<>();
        response.setContent(List.of(cartMapper.mapToCartDTO(cart)));
        return response;
    }

    @Override
    public BaseResponse<CartDTO> addItemToCart(CartItemDTO cartItemDTO) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    newCart.setItems(new ArrayList<>());
                    return cartRepository.save(newCart);
                });

        if (cartItemDTO.getProductId() == null) {
            throw new BadRequestException("Product ID không được để trống");
        }

        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sản phẩm không tìm thấy với id: " + cartItemDTO.getProductId()));

        if (!product.isAvailability()) {
            throw new BadRequestException("Sản phẩm " + product.getName() + " hiện không có sẵn");
        }

        if (cartItemDTO.getQuantity() <= 0 || cartItemDTO.getQuantity() > product.getQuantity()) {
            throw new BadRequestException("Số lượng không hợp lệ. Số lượng tối đa: " + product.getQuantity());
        }

        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(),
                cartItemDTO.getProductId());
        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + cartItemDTO.getQuantity();
            if (newQuantity > product.getQuantity()) {
                throw new BadRequestException("Số lượng vượt quá tồn kho: " + product.getQuantity());
            }
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(cartItemDTO.getQuantity());
            cartItemRepository.save(cartItem);
            cart.getItems().add(cartItem);
        }

        BaseResponse<CartDTO> response = new BaseResponse<>();
        response.setContent(List.of(cartMapper.mapToCartDTO(cart)));
        response.setMessage("Thêm sản phẩm vào giỏ hàng thành công.");
        return response;
    }

    @Override
    public BaseResponse<CartDTO> updateCartItem(Long itemId, CartItemDTO cartItemDTO) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục giỏ hàng không tìm thấy với id: " + itemId));

        Product product = cartItem.getProduct();
        if (!product.isAvailability()) {
            throw new BadRequestException("Sản phẩm " + product.getName() + " hiện không có sẵn");
        }

        if (cartItemDTO.getQuantity() <= 0 || cartItemDTO.getQuantity() > product.getQuantity()) {
            throw new BadRequestException("Số lượng không hợp lệ. Số lượng tối đa: " + product.getQuantity());
        }

        cartItem.setQuantity(cartItemDTO.getQuantity());
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        BaseResponse<CartDTO> response = new BaseResponse<>();
        response.setContent(List.of(cartMapper.mapToCartDTO(cart)));
        return response;
    }

    @Override
    public BaseResponse<CartDTO> removeItemFromCart(Long itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục giỏ hàng không tìm thấy với id: " + itemId));

        Cart cart = cartItem.getCart();
        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);

        BaseResponse<CartDTO> response = new BaseResponse<>();
        response.setMessage("Xóa sản phẩm khỏi giỏ hàng thành công.");
        response.setContent(List.of(cartMapper.mapToCartDTO(cart)));
        return response;
    }

    @Override
    public void clearCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Giỏ hàng không tìm thấy"));

        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear(); // Sử dụng clear() thay vì gán collection mới
        cartRepository.save(cart);
    }
}