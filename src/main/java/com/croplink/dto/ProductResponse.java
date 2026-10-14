package com.croplink.dto;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
@Data @Builder
public class ProductResponse {
    private Long id;
    private Long farmerId;
    private String farmerName;
    private String farmName;
    private Long categoryId;
    private String categoryName;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer quantityAvailable;
    private String unit;
    private String qualityGrade;
    private String ripenessLevel;
    private LocalDate harvestDate;
    private String freshnessStatus;
    private String organicStatus;
    private String location;
    private String deliveryOptions;
    private String paymentOptions;
    private Integer minimumOrderQuantity;
    private String availability;
    private String productStatus;
    private Double averageRating;
    private Integer totalReviews;
    private List<String> imageUrls;
    private String primaryImageUrl;
    private LocalDateTime createdAt;
}
