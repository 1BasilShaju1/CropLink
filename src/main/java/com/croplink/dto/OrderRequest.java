package com.croplink.dto;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class OrderRequest {
    @NotBlank private String deliveryMethod;
    @NotBlank private String paymentMethod;
    private String deliveryAddress;
}
