package com.croplink.dto;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
@Data @Builder
public class ReviewResponse {
    private Long id;
    private Long customerId;
    private String customerName;
    private Long productId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
