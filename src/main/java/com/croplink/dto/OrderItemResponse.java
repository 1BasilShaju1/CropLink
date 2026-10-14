package com.croplink.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
@Data @Builder
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
