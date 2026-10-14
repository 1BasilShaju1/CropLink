package com.croplink.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
@Data @Builder
public class OrderResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long farmerId;
    private String farmerName;
    private String deliveryPartnerName;
    private BigDecimal totalAmount;
    private BigDecimal deliveryFee;
    private BigDecimal finalAmount;
    private String deliveryMethod;
    private String paymentMethod;
    private String paymentStatus;
    private String deliveryAddress;
    private String orderStatus;
    private LocalDateTime orderDate;
    private LocalDateTime deliveredDate;
    private List<OrderItemResponse> orderItems;
}
