package com.croplink.controller;

import com.croplink.dto.*;
import com.croplink.security.CustomUserDetails;
import com.croplink.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/customer/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @AuthenticationPrincipal CustomUserDetails user,
            @Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Order placed", orderService.placeOrder(user.getId(), request)));
    }

    @GetMapping("/customer/orders")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getCustomerOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }
        Page<OrderResponse> orders = orderService.getCustomerOrders(user.getId(), page, size);
        if (orders.isEmpty() && user.getUser().getRole() == com.croplink.entity.User.Role.ADMIN) {
            orders = orderService.getAllOrders(page, size);
        }
        return ResponseEntity.ok(ApiResponse.success("Orders fetched", orders));
    }

    @GetMapping("/customer/orders/{id}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user) {
        return ResponseEntity.ok(ApiResponse.success("Order fetched", orderService.getOrderById(id)));
    }

    @GetMapping("/farmer/orders")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getFarmerOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }
        Page<OrderResponse> orders;
        if (user.getUser().getRole() == com.croplink.entity.User.Role.ADMIN) {
            orders = orderService.getAllOrders(page, size);
        } else {
            orders = orderService.getFarmerOrders(user.getId(), page, size);
        }
        return ResponseEntity.ok(ApiResponse.success("Orders fetched", orders));
    }

    @PatchMapping("/farmer/orders/{id}/status")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateFarmerOrderStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody Map<String, String> body) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }
        return ResponseEntity.ok(ApiResponse.success("Status updated", orderService.updateOrderStatus(id, body.get("status"), user.getId())));
    }

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("All orders",
                orderService.getAllOrders(page, size)));
    }
}
