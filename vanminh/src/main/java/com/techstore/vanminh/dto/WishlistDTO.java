package com.techstore.vanminh.dto;

import lombok.Data;
import java.util.List;

@Data
public class WishlistDTO {
    private Long id;
    private Long userId;
    private List<WishlistItemDTO> items;
}