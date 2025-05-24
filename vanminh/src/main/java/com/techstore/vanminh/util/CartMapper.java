package com.techstore.vanminh.util;

import com.techstore.vanminh.dto.CartDTO;
import com.techstore.vanminh.dto.CartItemDTO;
import com.techstore.vanminh.entity.Cart;
import com.techstore.vanminh.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CartMapper {

    public CartDTO mapToCartDTO(Cart cart) {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setId(cart.getId());
        cartDTO.setUserId(cart.getUser() != null ? cart.getUser().getId() : null);
        List<CartItemDTO> itemDTOs = new ArrayList<>();
        double totalCartPrice = 0.0;

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

        for (CartItem item : cart.getItems()) {
            CartItemDTO itemDTO = new CartItemDTO();
            itemDTO.setId(item.getId());
            itemDTO.setCartId(cart.getId());
            itemDTO.setProductId(item.getProduct() != null ? item.getProduct().getId() : null);
            itemDTO.setProductName(item.getProduct() != null ? item.getProduct().getName() : null);
            itemDTO.setProductPrice(item.getProduct() != null ? item.getProduct().getPrice() : null);
            itemDTO.setQuantity(item.getQuantity());
            itemDTO.setTotalPrice(item.getProduct() != null ? item.getProduct().getPrice() * item.getQuantity() : 0.0);
            itemDTOs.add(itemDTO);
            totalCartPrice += itemDTO.getTotalPrice();
        }

        cartDTO.setItems(itemDTOs);
        cartDTO.setTotalCartPrice(totalCartPrice);
        return cartDTO;
    }
}