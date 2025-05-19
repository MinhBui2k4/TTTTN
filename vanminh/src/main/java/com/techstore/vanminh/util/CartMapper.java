package com.techstore.vanminh.util;

import com.techstore.vanminh.dto.CartDTO;
import com.techstore.vanminh.dto.CartItemDTO;
import com.techstore.vanminh.entity.Cart;
import com.techstore.vanminh.entity.CartItem;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CartMapper {

    @Autowired
    private ModelMapper modelMapper;

    public CartDTO mapToCartDTO(Cart cart) {
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItemDTO> itemDTOs = new ArrayList<>();
        double totalCartPrice = 0.0;

        if (cart.getItems() == null) {
            cart.setItems(new ArrayList<>());
        }

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
}