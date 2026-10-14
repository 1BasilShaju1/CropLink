package com.croplink.repository;

import com.croplink.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByFarmerIdAndProductStatus(Long farmerId, String status, Pageable pageable);

    Page<Product> findByProductStatus(String status, Pageable pageable);

    Page<Product> findByFarmerId(Long farmerId, Pageable pageable);

    List<Product> findByFarmerId(Long farmerId);

    @Query("SELECT p FROM Product p WHERE p.productStatus = 'ACTIVE' AND " +
           "(:category IS NULL OR p.category.id = :category) AND " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findActiveProducts(@Param("category") Long category,
                                      @Param("search") String search,
                                      Pageable pageable);

    long countByFarmerIdAndProductStatus(Long farmerId, String status);

    long countByProductStatus(String status);
}
