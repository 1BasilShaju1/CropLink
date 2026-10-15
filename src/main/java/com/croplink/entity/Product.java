package com.croplink.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "farmer_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User farmer;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Category category;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "quantity_available")
    private Integer quantityAvailable;

    private String unit;

    @Column(name = "quality_grade")
    private String qualityGrade;

    @Column(name = "ripeness_level")
    private String ripenessLevel;

    @Column(name = "harvest_date")
    private LocalDate harvestDate;

    @Column(name = "freshness_status")
    private String freshnessStatus;

    @Column(name = "organic_status")
    private String organicStatus;

    private String location;

    @Column(name = "delivery_options")
    private String deliveryOptions;

    @Column(name = "payment_options")
    private String paymentOptions;

    @Column(name = "minimum_order_quantity")
    @Builder.Default
    private Integer minimumOrderQuantity = 1;

    @Builder.Default
    private String availability = "Available";

    @Column(name = "product_status")
    @Builder.Default
    private String productStatus = "ACTIVE";

    @Column(name = "average_rating")
    @Builder.Default
    private Double averageRating = 0.0;

    @Column(name = "total_reviews")
    @Builder.Default
    private Integer totalReviews = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<ProductImage> images = new ArrayList<>();
}
