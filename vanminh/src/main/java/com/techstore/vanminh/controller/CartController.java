// package com.techstore.vanminh.controller;

// import com.techstore.vanminh.dto.CartDTO;
// import com.techstore.vanminh.dto.CartItemDTO;
// import com.techstore.vanminh.dto.response.BaseResponse;
// import com.techstore.vanminh.service.CartService;

// import java.util.List;

// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
// import org.springframework.web.bind.annotation.*;

// import jakarta.validation.Valid;

// @RestController
// @RequestMapping("/api/users/cart")
// @PreAuthorize("hasRole('ROLE_USER')")
// public class CartController {

//     @Autowired
//     private CartService cartService;

//     @GetMapping
//     public ResponseEntity<BaseResponse<CartDTO>> getCart() {
//         return ResponseEntity.ok(cartService.getCart());
//     }

//     @PostMapping("/items")
//     public ResponseEntity<BaseResponse<CartDTO>> addItemToCart(@Valid @RequestBody CartItemDTO cartItemDTO) {
//         return ResponseEntity.ok(cartService.addItemToCart(cartItemDTO));
//     }

//     @PutMapping("/items/{itemId}")
//     public ResponseEntity<BaseResponse<CartDTO>> updateCartItem(@PathVariable Long itemId,
//             @Valid @RequestBody CartItemDTO cartItemDTO) {
//         return ResponseEntity.ok(cartService.updateCartItem(itemId, cartItemDTO));
//     }

//     @DeleteMapping("/items/{itemId}")
//     public ResponseEntity<BaseResponse<CartDTO>> removeItemFromCart(@PathVariable Long itemId) {
//         BaseResponse<CartDTO> response = cartService.removeItemFromCart(itemId);
//         response.setMessage("Xóa sản phẩm khỏi giỏ hàng thành công.");
//         return ResponseEntity.ok(response);
//     }

//     @DeleteMapping
//     public ResponseEntity<BaseResponse<String>> clearCart() {
//         cartService.clearCart();
//         BaseResponse<String> response = new BaseResponse<>();
//         response.setMessage("Đã xóa toàn bộ giỏ hàng.");
//         response.setContent(List.of("OK"));
//         return ResponseEntity.ok(response);
//     }

// }

package com.techstore.vanminh.controller;

import com.techstore.vanminh.dto.CartDTO;
import com.techstore.vanminh.dto.CartItemDTO;
import com.techstore.vanminh.dto.response.BaseResponse;
import com.techstore.vanminh.service.CartService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users/cart")
@PreAuthorize("hasRole('ROLE_USER')")
public class CartController {

    @Autowired
    private CartService cartService;

    @GetMapping
    public ResponseEntity<BaseResponse<CartDTO>> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PostMapping("/items")
    public ResponseEntity<BaseResponse<CartDTO>> addItemToCart(@Valid @RequestBody CartItemDTO cartItemDTO) {
        return ResponseEntity.ok(cartService.addItemToCart(cartItemDTO));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<BaseResponse<CartDTO>> updateCartItem(@PathVariable Long itemId,
            @Valid @RequestBody CartItemDTO cartItemDTO) {
        return ResponseEntity.ok(cartService.updateCartItem(itemId, cartItemDTO));
    }

    // @DeleteMapping("/items/{itemId}")
    // public ResponseEntity<BaseResponse<CartDTO>> removeItemFromCart(@PathVariable
    // Long itemId) {
    // return ResponseEntity.ok(cartService.removeItemFromCart(itemId));
    // }

    // @DeleteMapping
    // public ResponseEntity<Void> clearCart() {
    // cartService.clearCart();
    // return ResponseEntity.noContent().build();
    // }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<BaseResponse<CartDTO>> removeItemFromCart(@PathVariable Long itemId) {
        BaseResponse<CartDTO> response = cartService.removeItemFromCart(itemId);
        response.setMessage("Xóa sản phẩm khỏi giỏ hàng thành công.");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<BaseResponse<String>> clearCart() {
        cartService.clearCart();
        BaseResponse<String> response = new BaseResponse<>();
        response.setMessage("Đã xóa toàn bộ giỏ hàng.");
        response.setContent(List.of("OK"));
        return ResponseEntity.ok(response);
    }
}