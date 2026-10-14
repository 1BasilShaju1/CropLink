package com.croplink.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class RegisterRequest {
    @NotBlank private String fullName;
    @Email @NotBlank private String email;
    @NotBlank @Size(min=6) private String password;
    private String phone;
    @NotBlank private String role; // FARMER, CUSTOMER, DELIVERY_PARTNER
    // Farmer fields
    private String farmName;
    private String address;
    private String village;
    private String district;
    private String state;
    private String pincode;
    // Customer fields
    private String customerAddress;
    // Delivery partner fields
    private String vehicleType;
    private String serviceArea;
}
