package com.croplink.service;

import com.croplink.dto.OrderItemResponse;
import com.croplink.dto.OrderRequest;
import com.croplink.dto.OrderResponse;
import com.croplink.entity.*;
import com.croplink.exception.AppException;
import com.croplink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public OrderResponse placeOrder(Long customerId, OrderRequest request) {
        Cart cart = cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    User customer = userRepository.findById(customerId)
                            .orElseThrow(() -> new AppException("Customer not found", 404));
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });
        if (cart.getItems().isEmpty()) throw new AppException("Cart is empty", 400);
        CartItem firstItem = cart.getItems().get(0);
        User farmer = (firstItem.getProduct() != null && firstItem.getProduct().getFarmer() != null)
                ? firstItem.getProduct().getFarmer()
                : userRepository.findByEmail("farmer@croplink.com").orElse(null);
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            if (ci.getProduct() != null && ci.getProduct().getPrice() != null) {
                total = total.add(ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
            }
        }
        BigDecimal deliveryFee = "Farm Pickup".equals(request.getDeliveryMethod()) ? BigDecimal.ZERO : new BigDecimal("40");
        BigDecimal finalAmount = total.add(deliveryFee);
        User customer = userRepository.findById(customerId).orElseThrow(() -> new AppException("Customer not found", 404));
        Order order = Order.builder()
                .customer(customer)
                .farmer(farmer != null ? farmer : customer)
                .totalAmount(total)
                .deliveryFee(deliveryFee)
                .finalAmount(finalAmount)
                .deliveryMethod(request.getDeliveryMethod() != null ? request.getDeliveryMethod() : "Standard Delivery")
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "Cash on Delivery")
                .paymentStatus("PENDING")
                .deliveryAddress(request.getDeliveryAddress() != null ? request.getDeliveryAddress() : "")
                .orderStatus("PENDING")
                .build();
        order = orderRepository.save(order);
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            if (p == null) continue;
            int currentStock = p.getQuantityAvailable() != null ? p.getQuantityAvailable() : 999;
            if (currentStock < ci.getQuantity()) {
                throw new AppException("Insufficient stock for: " + p.getName(), 400);
            }
            p.setQuantityAvailable(Math.max(0, currentStock - ci.getQuantity()));
            productRepository.save(p);
            OrderItem oi = OrderItem.builder()
                    .order(order)
                    .product(p)
                    .productName(p.getName())
                    .quantity(ci.getQuantity())
                    .unitPrice(p.getPrice())
                    .subtotal(p.getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())))
                    .build();
            orderItems.add(orderItemRepository.save(oi));
        }
        cartItemRepository.deleteByCartId(cart.getId());
        Payment payment = Payment.builder()
                .order(order)
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("PENDING")
                .amount(finalAmount)
                .build();
        paymentRepository.save(payment);
        notificationService.sendNotification(customerId, "Order Placed!", "Your order #" + order.getId() + " has been placed successfully.", "ORDER");
        notificationService.sendNotification(farmer.getId(), "New Order!", "You have a new order #" + order.getId() + " from " + customer.getFullName(), "ORDER");
        return toResponse(order, orderItems);
    }

    public Page<OrderResponse> getCustomerOrders(Long customerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return orderRepository.findByCustomerId(customerId, pageable)
                .map(o -> toResponse(o, orderItemRepository.findByOrderId(o.getId())));
    }

    public Page<OrderResponse> getFarmerOrders(Long farmerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return orderRepository.findByFarmerId(farmerId, pageable)
                .map(o -> toResponse(o, orderItemRepository.findByOrderId(o.getId())));
    }

    public Page<OrderResponse> getDeliveryPartnerOrders(Long partnerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return orderRepository.findByDeliveryPartnerId(partnerId, pageable)
                .map(o -> toResponse(o, orderItemRepository.findByOrderId(o.getId())));
    }

    public Page<OrderResponse> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("orderDate").descending());
        return orderRepository.findAll(pageable)
                .map(o -> toResponse(o, orderItemRepository.findByOrderId(o.getId())));
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, String status, Long actorId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException("Order not found", 404));
        order.setOrderStatus(status);
        if ("DELIVERED".equals(status)) {
            order.setDeliveredDate(java.time.LocalDateTime.now());
            order.setPaymentStatus("PAID");
            paymentRepository.findByOrderId(orderId).ifPresent(p -> {
                p.setPaymentStatus("PAID");
                paymentRepository.save(p);
            });
        }
        order = orderRepository.save(order);
        notificationService.sendNotification(order.getCustomer().getId(), "Order Update",
                "Your order #" + orderId + " is now " + status, "ORDER");
        return toResponse(order, orderItemRepository.findByOrderId(orderId));
    }

    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException("Order not found", 404));
        return toOrderResponse(order);
    }

    public OrderResponse toOrderResponse(Order o) {
        if (o == null) return null;
        List<OrderItem> items = orderItemRepository.findByOrderId(o.getId());
        return toResponse(o, items);
    }

    public OrderResponse toResponse(Order o, List<OrderItem> items) {
        if (o == null) return null;
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        if (items != null) {
            for (OrderItem oi : items) {
                Long prodId = null;
                String prodName = oi.getProductName();
                try {
                    if (oi.getProduct() != null) {
                        prodId = oi.getProduct().getId();
                        if (prodName == null || prodName.isBlank()) {
                            prodName = oi.getProduct().getName();
                        }
                    }
                } catch (Exception ignored) {}
                if (prodName == null || prodName.isBlank()) prodName = "Fresh Produce";

                itemResponses.add(OrderItemResponse.builder()
                        .id(oi.getId())
                        .productId(prodId)
                        .productName(prodName)
                        .quantity(oi.getQuantity() != null ? oi.getQuantity() : 1)
                        .unitPrice(oi.getUnitPrice() != null ? oi.getUnitPrice() : BigDecimal.ZERO)
                        .subtotal(oi.getSubtotal() != null ? oi.getSubtotal() : BigDecimal.ZERO)
                        .build());
            }
        }

        Long customerId = null;
        String customerName = "Customer";
        try {
            if (o.getCustomer() != null) {
                customerId = o.getCustomer().getId();
                if (o.getCustomer().getFullName() != null) {
                    customerName = o.getCustomer().getFullName();
                }
            }
        } catch (Exception ignored) {}

        Long farmerId = null;
        String farmerName = "Farmer";
        try {
            if (o.getFarmer() != null) {
                farmerId = o.getFarmer().getId();
                if (o.getFarmer().getFullName() != null) {
                    farmerName = o.getFarmer().getFullName();
                }
            }
        } catch (Exception ignored) {}

        String deliveryPartnerName = null;
        try {
            if (o.getDeliveryPartner() != null) {
                deliveryPartnerName = o.getDeliveryPartner().getFullName();
            }
        } catch (Exception ignored) {}

        return OrderResponse.builder()
                .id(o.getId())
                .customerId(customerId)
                .customerName(customerName)
                .farmerId(farmerId)
                .farmerName(farmerName)
                .deliveryPartnerName(deliveryPartnerName)
                .totalAmount(o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO)
                .deliveryFee(o.getDeliveryFee() != null ? o.getDeliveryFee() : BigDecimal.ZERO)
                .finalAmount(o.getFinalAmount() != null ? o.getFinalAmount() : BigDecimal.ZERO)
                .deliveryMethod(o.getDeliveryMethod() != null ? o.getDeliveryMethod() : "Standard Delivery")
                .paymentMethod(o.getPaymentMethod() != null ? o.getPaymentMethod() : "Cash on Delivery")
                .paymentStatus(o.getPaymentStatus() != null ? o.getPaymentStatus() : "PENDING")
                .deliveryAddress(o.getDeliveryAddress() != null ? o.getDeliveryAddress() : "")
                .orderStatus(o.getOrderStatus() != null ? o.getOrderStatus() : "PENDING")
                .orderDate(o.getOrderDate())
                .deliveredDate(o.getDeliveredDate())
                .orderItems(itemResponses)
                .build();
    }
}
