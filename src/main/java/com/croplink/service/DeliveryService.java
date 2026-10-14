package com.croplink.service;

import com.croplink.entity.Order;
import com.croplink.entity.User;
import com.croplink.exception.AppException;
import com.croplink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeliveryService {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    /**
     * Find all orders with status PACKED and no delivery partner assigned (available for pickup).
     */
    public Page<Order> getAvailableDeliveries(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return orderRepository.findPackedOrdersWithoutPartner(pageable);
    }

    @Transactional
    public Order acceptDelivery(Long orderId, Long partnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Order not found", 404));
        if (!"PACKED".equals(order.getOrderStatus())) {
            throw new AppException("Order not available for pickup", 400);
        }
        if (order.getDeliveryPartner() != null) {
            throw new AppException("Order already assigned to a delivery partner", 400);
        }
        User partner = userRepository.findById(partnerId)
                .orElseThrow(() -> new AppException("Partner not found", 404));
        order.setDeliveryPartner(partner);
        order.setOrderStatus("OUT_FOR_DELIVERY");
        order = orderRepository.save(order);
        notificationService.sendNotification(order.getCustomer().getId(),
                "Out for Delivery",
                "Your order #" + orderId + " is out for delivery!",
                "ORDER");
        return order;
    }

    @Transactional
    public Order updateDeliveryStatus(Long orderId, String status, Long partnerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Order not found", 404));
        if (order.getDeliveryPartner() == null || !order.getDeliveryPartner().getId().equals(partnerId)) {
            throw new AppException("Not your delivery", 403);
        }
        order.setOrderStatus(status);
        if ("DELIVERED".equals(status)) {
            order.setDeliveredDate(java.time.LocalDateTime.now());
            order.setPaymentStatus("PAID");
        }
        order = orderRepository.save(order);
        notificationService.sendNotification(order.getCustomer().getId(),
                "Order " + status,
                "Your order #" + orderId + " status updated to " + status,
                "ORDER");
        return order;
    }
}
