package com.croplink.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
@Data @Builder
public class CartItemResponse {
    private Long cartItemId;
    private Long productId;
    private String productName;
    private String farmerName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal subtotal;
    private String unit;
    private String imageUrl;
    private Integer stockAvailable;
}
