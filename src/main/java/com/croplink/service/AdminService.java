package com.croplink.service;

import com.croplink.dto.DashboardStats;
import com.croplink.entity.User;
import com.croplink.exception.AppException;
import com.croplink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdminService {
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final CategoryRepository categoryRepository;

    public DashboardStats getDashboardStats() {
        long totalProducts = productRepository.count();
        long activeProducts = productRepository.countByProductStatus("ACTIVE");
        long totalOrders = orderRepository.count();
        long pendingOrders = orderRepository.countByOrderStatus("PENDING");
        long deliveredOrders = orderRepository.countByOrderStatus("DELIVERED");
        BigDecimal revenue = orderRepository.totalRevenue();
        long totalUsers = userRepository.count();
        long farmers = userRepository.countByRole(User.Role.FARMER);
        long customers = userRepository.countByRole(User.Role.CUSTOMER);
        return DashboardStats.builder()
                .totalProducts(totalProducts)
                .activeProducts(activeProducts)
                .totalOrders(totalOrders)
                .pendingOrders(pendingOrders)
                .deliveredOrders(deliveredOrders)
                .totalRevenue(revenue != null ? revenue : BigDecimal.ZERO)
                .totalUsers(totalUsers)
                .totalFarmers(farmers)
                .totalCustomers(customers)
                .build();
    }

    public Page<User> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
    }

    public User updateUserStatus(Long userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException("User not found", 404));
        user.setStatus(User.UserStatus.valueOf(status));
        return userRepository.save(user);
    }
}
