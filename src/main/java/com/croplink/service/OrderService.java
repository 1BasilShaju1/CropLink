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
        User farmer = firstItem.getProduct().getFarmer();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cart.getItems()) {
            total = total.add(ci.getProduct().getPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        BigDecimal deliveryFee = "Farm Pickup".equals(request.getDeliveryMethod()) ? BigDecimal.ZERO : new BigDecimal("40");
        BigDecimal finalAmount = total.add(deliveryFee);
        User customer = userRepository.findById(customerId).orElseThrow(() -> new AppException("Customer not found", 404));
        Order order = Order.builder()
                .customer(customer)
                .farmer(farmer)
                .totalAmount(total)
                .deliveryFee(deliveryFee)
                .finalAmount(finalAmount)
                .deliveryMethod(request.getDeliveryMethod())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus("PENDING")
                .deliveryAddress(request.getDeliveryAddress())
                .orderStatus("PENDING")
                .build();
        order = orderRepository.save(order);
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem ci : cart.getItems()) {
            Product p = ci.getProduct();
            if (p.getQuantityAvailable() < ci.getQuantity()) {
                throw new AppException("Insufficient stock for: " + p.getName(), 400);
            }
            p.setQuantityAvailable(p.getQuantityAvailable() - ci.getQuantity());
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
        List<OrderItemResponse> itemResponses = items.stream().map(oi -> OrderItemResponse.builder()
                .id(oi.getId())
                .productId(oi.getProduct().getId())
                .productName(oi.getProductName())
                .quantity(oi.getQuantity())
                .unitPrice(oi.getUnitPrice())
                .subtotal(oi.getSubtotal())
                .build()
        ).collect(Collectors.toList());
        return OrderResponse.builder()
                .id(o.getId())
                .customerId(o.getCustomer().getId())
                .customerName(o.getCustomer().getFullName())
                .farmerId(o.getFarmer().getId())
                .farmerName(o.getFarmer().getFullName())
                .deliveryPartnerName(o.getDeliveryPartner() != null ? o.getDeliveryPartner().getFullName() : null)
                .totalAmount(o.getTotalAmount())
                .deliveryFee(o.getDeliveryFee())
                .finalAmount(o.getFinalAmount())
                .deliveryMethod(o.getDeliveryMethod())
                .paymentMethod(o.getPaymentMethod())
                .paymentStatus(o.getPaymentStatus())
                .deliveryAddress(o.getDeliveryAddress())
                .orderStatus(o.getOrderStatus())
                .orderDate(o.getOrderDate())
                .deliveredDate(o.getDeliveredDate())
                .orderItems(itemResponses)
                .build();
    }
}
