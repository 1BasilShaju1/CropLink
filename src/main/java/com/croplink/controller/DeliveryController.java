package com.croplink.controller;

import com.croplink.dto.ApiResponse;
import com.croplink.dto.OrderResponse;
import com.croplink.entity.Order;
import com.croplink.security.CustomUserDetails;
import com.croplink.service.DeliveryService;
import com.croplink.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
@PreAuthorize("hasRole('DELIVERY_PARTNER')")
public class DeliveryController {
    private final DeliveryService deliveryService;
    private final OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success("Delivery orders",
                orderService.getDeliveryPartnerOrders(user.getId(), page, size)));
    }

    @GetMapping("/available")
    public ResponseEntity<?> getAvailableDeliveries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Order> orders = deliveryService.getAvailableDeliveries(page, size);
        return ResponseEntity.ok(ApiResponse.success("Available deliveries", orders));
    }

    @PostMapping("/orders/{orderId}/accept")
    public ResponseEntity<?> acceptDelivery(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails user) {
        Order order = deliveryService.acceptDelivery(orderId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Delivery accepted", order));
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody Map<String, String> body) {
        Order order = deliveryService.updateDeliveryStatus(orderId, body.get("status"), user.getId());
        return ResponseEntity.ok(ApiResponse.success("Status updated", order));
    }
}
