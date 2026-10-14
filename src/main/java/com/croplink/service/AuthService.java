package com.croplink.service;

import com.croplink.dto.AuthRequest;
import com.croplink.dto.AuthResponse;
import com.croplink.dto.RegisterRequest;
import com.croplink.entity.*;
import com.croplink.exception.AppException;
import com.croplink.repository.*;
import com.croplink.security.CustomUserDetails;
import com.croplink.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final FarmerProfileRepository farmerProfileRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final DeliveryPartnerProfileRepository deliveryPartnerProfileRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException("Email already registered", 400);
        }
        User.Role role;
        try {
            role = User.Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException("Invalid role: " + request.getRole(), 400);
        }
        if (role == User.Role.ADMIN) {
            throw new AppException("Cannot register as ADMIN", 403);
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(role)
                .status(User.UserStatus.ACTIVE)
                .build();
        user = userRepository.save(user);

        if (role == User.Role.FARMER) {
            FarmerProfile profile = FarmerProfile.builder()
                    .user(user)
                    .farmName(request.getFarmName())
                    .address(request.getAddress())
                    .village(request.getVillage())
                    .district(request.getDistrict())
                    .state(request.getState())
                    .pincode(request.getPincode())
                    .build();
            farmerProfileRepository.save(profile);
        } else if (role == User.Role.CUSTOMER) {
            CustomerProfile profile = CustomerProfile.builder()
                    .user(user)
                    .address(request.getCustomerAddress())
                    .district(request.getDistrict())
                    .state(request.getState())
                    .pincode(request.getPincode())
                    .build();
            customerProfileRepository.save(profile);
            Cart cart = new Cart();
            cart.setCustomer(user);
            cartRepository.save(cart);
        } else if (role == User.Role.DELIVERY_PARTNER) {
            DeliveryPartnerProfile profile = DeliveryPartnerProfile.builder()
                    .user(user)
                    .vehicleType(request.getVehicleType())
                    .serviceArea(request.getServiceArea())
                    .status("AVAILABLE")
                    .build();
            deliveryPartnerProfileRepository.save(profile);
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails);
        return AuthResponse.builder()
                .token(token)
                .role(user.getRole().name())
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .build();
    }

    public AuthResponse login(AuthRequest request) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail().toLowerCase(), request.getPassword())
        );
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        return AuthResponse.builder()
                .token(token)
                .role(userDetails.getUser().getRole().name())
                .userId(userDetails.getId())
                .fullName(userDetails.getUser().getFullName())
                .email(userDetails.getUsername())
                .build();
    }
}
