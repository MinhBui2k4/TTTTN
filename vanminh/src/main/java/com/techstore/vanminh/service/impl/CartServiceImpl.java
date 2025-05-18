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

import org.modelmapper.ModelMapper;
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
    private ModelMapper modelMapper;

    private CartDTO mapToCartDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItemDTO> itemDTOs = new ArrayList<>();
        double totalCartPrice = 0.0;

        for (CartItem item : cart.getItems()) {
            CartItemDTO itemDTO = modelMapper.map(item, CartItemDTO.class);
            itemDTO.setProductName(item.getProduct().getName());
            itemDTO.setProductPrice(item.getProduct().getPrice());
            itemDTO.setTotalPrice(item.getProduct().getPrice() * item.getQuantity());
            itemDTOs.add(itemDTO);
            totalCartPrice += itemDTO.getTotalPrice();
        }

        cartDTO.setItems(itemDTOs);
        cartDTO.setTotalCartPrice(totalCartPrice);
        return cartDTO;
    }

    @Override
    public BaseResponse<CartDTO> getCart() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Người dùng không tìm thấy"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        BaseResponse<CartDTO> response = new BaseResponse<>();
        response.setContent(List.of(mapToCartDTO(cart)));
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
                    return cartRepository.save(newCart);
                });

        Product product = productRepository.findById(cartItemDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Sản phẩm không tìm thấy với id: " + cartItemDTO.getProductId()));

        if (!product.isAvailability()) {
            throw new BadRequestException("Sản phẩm hiện không có sẵn");
        }

        if (cartItemDTO.getQuantity() <= 0 || cartItemDTO.getQuantity() > product.getQuantity()) {
            throw new BadRequestException("Số lượng không hợp lệ. Số lượng tối đa: " + product.getQuantity());
        }

        // Kiểm tra xem sản phẩm đã có trong giỏ chưa
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
        }

        BaseResponse<CartDTO> response = new BaseResponse<>();
        response.setContent(List.of(mapToCartDTO(cart)));
        return response;
    }

    @Override
    public BaseResponse<CartDTO> updateCartItem(Long itemId, CartItemDTO cartItemDTO) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục giỏ hàng không tìm thấy với id: " + itemId));

        Product product = cartItem.getProduct();
        if (!product.isAvailability()) {
            throw new BadRequestException("Sản phẩm hiện không có sẵn");
        }

        if (cartItemDTO.getQuantity() <= 0 || cartItemDTO.getQuantity() > product.getQuantity()) {
            throw new BadRequestException("Số lượng không hợp lệ. Số lượng tối đa: " + product.getQuantity());
        }

        cartItem.setQuantity(cartItemDTO.getQuantity());
        cartItemRepository.save(cartItem);

        Cart cart = cartItem.getCart();
        BaseResponse<CartDTO> response = new BaseResponse<>();
        response.setContent(List.of(mapToCartDTO(cart)));
        return response;
    }

    @Override
    public BaseResponse<CartDTO> removeItemFromCart(Long itemId) {
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Mục giỏ hàng không tìm thấy với id: " + itemId));

        Cart cart = cartItem.getCart();

        // XÓA KHỎI LIST TRONG CART ĐỂ ĐỒNG BỘ BỘ NHỚ & DB
        cart.getItems().remove(cartItem);

        // XÓA KHỎI DATABASE
        cartItemRepository.delete(cartItem);

        BaseResponse<CartDTO> response = new BaseResponse<>();
        response.setMessage("Xóa sản phẩm khỏi giỏ hàng thành công.");
        response.setContent(List.of(mapToCartDTO(cart)));
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
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}