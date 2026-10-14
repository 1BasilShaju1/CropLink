package com.croplink.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
@Data @Builder
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
    private long unreadNotifications;
}
