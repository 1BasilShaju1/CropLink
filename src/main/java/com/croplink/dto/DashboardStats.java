package com.croplink.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStats {
    private long totalProducts;
    private long activeProducts;
    private long totalOrders;
    private long pendingOrders;
    private long deliveredOrders;
    private BigDecimal totalRevenue;
    private long totalUsers;
    private long totalFarmers;
    private long totalCustomers;
    private long pendingFarmers;
    private long unreadNotifications;
}
