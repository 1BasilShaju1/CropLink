package com.croplink.repository;

import com.croplink.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Page<Order> findByFarmerId(Long farmerId, Pageable pageable);

    Page<Order> findByDeliveryPartnerId(Long partnerId, Pageable pageable);

    List<Order> findByFarmerIdAndOrderStatus(Long farmerId, String status);

    long countByFarmerIdAndOrderStatus(Long farmerId, String status);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.farmer.id = :farmerId AND o.orderStatus = 'DELIVERED'")
    BigDecimal sumDeliveredAmountByFarmer(@Param("farmerId") Long farmerId);

    long countByOrderStatus(String status);

    @Query("SELECT COALESCE(SUM(o.finalAmount), 0) FROM Order o WHERE o.orderStatus = 'DELIVERED'")
    BigDecimal totalRevenue();

    @Query("SELECT o FROM Order o WHERE o.orderStatus = 'PACKED' AND o.deliveryPartner IS NULL")
    Page<Order> findPackedOrdersWithoutPartner(Pageable pageable);
}
