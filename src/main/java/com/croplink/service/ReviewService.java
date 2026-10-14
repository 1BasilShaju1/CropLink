package com.croplink.service;

import com.croplink.dto.ReviewRequest;
import com.croplink.dto.ReviewResponse;
import com.croplink.entity.Review;
import com.croplink.entity.User;
import com.croplink.exception.AppException;
import com.croplink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Transactional
    public ReviewResponse createReview(Long customerId, ReviewRequest request) {
        if (reviewRepository.existsByCustomerIdAndProductIdAndOrderId(customerId, request.getProductId(), request.getOrderId())) {
            throw new AppException("Already reviewed this product", 400);
        }
        User customer = userRepository.findById(customerId).orElseThrow(() -> new AppException("User not found", 404));
        var product = productRepository.findById(request.getProductId()).orElseThrow(() -> new AppException("Product not found", 404));
        var order = orderRepository.findById(request.getOrderId()).orElseThrow(() -> new AppException("Order not found", 404));
        Review review = Review.builder()
                .customer(customer).product(product).order(order)
                .rating(request.getRating()).comment(request.getComment()).build();
        reviewRepository.save(review);
        // Update product average rating
        Double avg = reviewRepository.findAverageRatingByProductId(product.getId());
        long count = reviewRepository.countByProductId(product.getId());
        product.setAverageRating(avg != null ? avg : 0.0);
        product.setTotalReviews((int) count);
        productRepository.save(product);
        return toResponse(review);
    }

    public List<ReviewResponse> getProductReviews(Long productId) {
        return reviewRepository.findByProductId(productId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder().id(r.getId()).customerId(r.getCustomer().getId())
                .customerName(r.getCustomer().getFullName()).productId(r.getProduct().getId())
                .rating(r.getRating()).comment(r.getComment()).createdAt(r.getCreatedAt()).build();
    }
}
