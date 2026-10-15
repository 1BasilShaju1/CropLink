package com.croplink.service;

import com.croplink.dto.CartItemRequest;
import com.croplink.dto.CartItemResponse;
import com.croplink.dto.CartResponse;
import com.croplink.entity.*;
import com.croplink.exception.AppException;
import com.croplink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;

    public Cart getOrCreateCart(Long customerId) {
        return cartRepository.findByCustomerId(customerId)
                .orElseGet(() -> {
                    User customer = userRepository.findById(customerId)
                            .orElseThrow(() -> new AppException("Customer not found", 404));
                    Cart newCart = new Cart();
                    newCart.setCustomer(customer);
                    return cartRepository.save(newCart);
                });
    }

    public CartResponse getCart(Long customerId) {
        Cart cart = getOrCreateCart(customerId);
        List<CartItemResponse> items = (cart.getItems() != null ? cart.getItems() : java.util.Collections.<CartItem>emptyList())
                .stream().map(this::toItemResponse).collect(Collectors.toList());
        BigDecimal total = items.stream().map(CartItemResponse::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponse.builder().cartId(cart.getId()).items(items).totalAmount(total).totalItems(items.size()).build();
    }

    @Transactional
    public CartResponse addItem(Long customerId, CartItemRequest request) {
        Cart cart = getOrCreateCart(customerId);
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException("Product not found", 404));
        if (!product.getProductStatus().equals("ACTIVE")) throw new AppException("Product not available", 400);
        cartItemRepository.findByCartIdAndProductId(cart.getId(), product.getId()).ifPresentOrElse(
            item -> {
                item.setQuantity(item.getQuantity() + request.getQuantity());
                cartItemRepository.save(item);
            },
            () -> {
                CartItem item = CartItem.builder().cart(cart).product(product).quantity(request.getQuantity()).build();
                cartItemRepository.save(item);
            }
        );
        return getCart(customerId);
    }

    @Transactional
    public CartResponse updateItem(Long customerId, Long cartItemId, Integer quantity) {
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow(() -> new AppException("Item not found", 404));
        if (!item.getCart().getCustomer().getId().equals(customerId)) throw new AppException("Not authorized", 403);
        if (quantity <= 0) cartItemRepository.delete(item);
        else { item.setQuantity(quantity); cartItemRepository.save(item); }
        return getCart(customerId);
    }

    @Transactional
    public CartResponse removeItem(Long customerId, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow(() -> new AppException("Item not found", 404));
        if (!item.getCart().getCustomer().getId().equals(customerId)) throw new AppException("Not authorized", 403);
        cartItemRepository.delete(item);
        return getCart(customerId);
    }

    @Transactional
    public void clearCart(Long customerId) {
        Cart cart = cartRepository.findByCustomerId(customerId).orElse(null);
        if (cart != null) cartItemRepository.deleteByCartId(cart.getId());
    }

    private CartItemResponse toItemResponse(CartItem item) {
        Product p = item.getProduct();
        List<ProductImage> images = productImageRepository.findByProductId(p.getId());
        String imageUrl = images.stream().filter(ProductImage::getIsPrimary).map(ProductImage::getImageUrl).findFirst()
                .orElse(images.isEmpty() ? null : images.get(0).getImageUrl());
        BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        return CartItemResponse.builder()
                .cartItemId(item.getId())
                .productId(p.getId())
                .productName(p.getName())
                .farmerName(p.getFarmer().getFullName())
                .unitPrice(p.getPrice())
                .quantity(item.getQuantity())
                .subtotal(subtotal)
                .unit(p.getUnit())
                .imageUrl(imageUrl)
                .stockAvailable(p.getQuantityAvailable())
                .build();
    }
}
