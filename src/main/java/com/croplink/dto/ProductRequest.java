package com.croplink.dto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class ProductRequest {
    @NotBlank private String name;
    @NotNull private Long categoryId;
    private String description;
    @NotNull @DecimalMin("0.01") private BigDecimal price;
    @NotNull @Min(0) private Integer quantityAvailable;
    @NotBlank private String unit;
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
}
