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
@PreAuthorize("hasRole('DELIVERY_PARTNER') or hasRole('ADMIN')")
public class DeliveryController {
    private final DeliveryService deliveryService;
    private final OrderService orderService;

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
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
            orders = orderService.getDeliveryPartnerOrders(user.getId(), page, size);
        }
        return ResponseEntity.ok(ApiResponse.success("Delivery orders", orders));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAvailableDeliveries(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<OrderResponse> orders = deliveryService.getAvailableDeliveries(page, size)
                .map(orderService::toOrderResponse);
        return ResponseEntity.ok(ApiResponse.success("Available deliveries", orders));
    }

    @PostMapping("/orders/{orderId}/accept")
    public ResponseEntity<ApiResponse<OrderResponse>> acceptDelivery(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails user) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }
        Order order = deliveryService.acceptDelivery(orderId, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Delivery accepted", orderService.toOrderResponse(order)));
    }

    @PatchMapping("/orders/{orderId}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable Long orderId,
            @AuthenticationPrincipal CustomUserDetails user,
            @RequestBody Map<String, String> body) {
        if (user == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }
        Order order = deliveryService.updateDeliveryStatus(orderId, body.get("status"), user.getId());
        return ResponseEntity.ok(ApiResponse.success("Status updated", orderService.toOrderResponse(order)));
    }
}
