package com.croplink.controller;

import com.croplink.dto.*;
import com.croplink.security.CustomUserDetails;
import com.croplink.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping({"/products", "/products/public"})
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getPublicProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String sort) {
        return ResponseEntity.ok(ApiResponse.success("Products fetched",
                productService.getActiveProducts(category, search, page, size, sort)));
    }

    @GetMapping({"/products/{id}", "/products/public/{id}"})
    public ResponseEntity<ApiResponse<ProductResponse>> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Product fetched", productService.getProductById(id)));
    }

    @GetMapping("/farmer/products")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getFarmerProducts(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }
        if (userDetails.getUser().getRole() == com.croplink.entity.User.Role.ADMIN) {
            return ResponseEntity.ok(ApiResponse.success("Products fetched",
                    productService.getAllProductsList()));
        }
        return ResponseEntity.ok(ApiResponse.success("Products fetched",
                productService.getFarmerProductsList(userDetails.getId())));
    }

    @PostMapping("/farmer/products")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestParam String name,
            @RequestParam Long categoryId,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam Integer quantityAvailable,
            @RequestParam String unit,
            @RequestParam(required = false) String qualityGrade,
            @RequestParam(required = false) String ripenessLevel,
            @RequestParam(required = false) String harvestDate,
            @RequestParam(required = false) String freshnessStatus,
            @RequestParam(required = false) String organicStatus,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String deliveryOptions,
            @RequestParam(required = false) String paymentOptions,
            @RequestParam(required = false) Integer minimumOrderQuantity,
            @RequestParam(required = false) String availability,
            @RequestParam(required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }
        ProductRequest req = new ProductRequest();
        req.setName(name); req.setCategoryId(categoryId); req.setDescription(description);
        req.setPrice(price); req.setQuantityAvailable(quantityAvailable); req.setUnit(unit);
        req.setQualityGrade(qualityGrade); req.setRipenessLevel(ripenessLevel);
        LocalDate parsedHarvestDate = LocalDate.now();
        if (harvestDate != null && !harvestDate.isBlank()) {
            try {
                parsedHarvestDate = LocalDate.parse(harvestDate);
            } catch (Exception ignored) {}
        }
        req.setHarvestDate(parsedHarvestDate);
        req.setFreshnessStatus(freshnessStatus); req.setOrganicStatus(organicStatus);
        req.setLocation(location); req.setDeliveryOptions(deliveryOptions);
        req.setPaymentOptions(paymentOptions);
        req.setMinimumOrderQuantity(minimumOrderQuantity != null ? minimumOrderQuantity : 1);
        req.setAvailability(availability != null && !availability.isBlank() ? availability : "Available");
        return ResponseEntity.ok(ApiResponse.success("Product created",
                productService.createProduct(req, images, userDetails.getId())));
    }

    @PutMapping("/farmer/products/{id}")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam Long categoryId,
            @RequestParam(required = false) String description,
            @RequestParam BigDecimal price,
            @RequestParam Integer quantityAvailable,
            @RequestParam String unit,
            @RequestParam(required = false) String qualityGrade,
            @RequestParam(required = false) String ripenessLevel,
            @RequestParam(required = false) String harvestDate,
            @RequestParam(required = false) String freshnessStatus,
            @RequestParam(required = false) String organicStatus,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String deliveryOptions,
            @RequestParam(required = false) String paymentOptions,
            @RequestParam(required = false) Integer minimumOrderQuantity,
            @RequestParam(required = false) String availability,
            @RequestParam(required = false) List<MultipartFile> images,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }
        ProductRequest req = new ProductRequest();
        req.setName(name); req.setCategoryId(categoryId); req.setDescription(description);
        req.setPrice(price); req.setQuantityAvailable(quantityAvailable); req.setUnit(unit);
        req.setQualityGrade(qualityGrade); req.setRipenessLevel(ripenessLevel);
        LocalDate parsedHarvestDate = LocalDate.now();
        if (harvestDate != null && !harvestDate.isBlank()) {
            try {
                parsedHarvestDate = LocalDate.parse(harvestDate);
            } catch (Exception ignored) {}
        }
        req.setHarvestDate(parsedHarvestDate);
        req.setFreshnessStatus(freshnessStatus); req.setOrganicStatus(organicStatus);
        req.setLocation(location); req.setDeliveryOptions(deliveryOptions);
        req.setPaymentOptions(paymentOptions);
        req.setMinimumOrderQuantity(minimumOrderQuantity != null ? minimumOrderQuantity : 1);
        req.setAvailability(availability != null && !availability.isBlank() ? availability : "Available");
        return ResponseEntity.ok(ApiResponse.success("Product updated",
                productService.updateProduct(id, req, images, userDetails.getId())));
    }

    @DeleteMapping("/farmer/products/{id}")
    @PreAuthorize("hasRole('FARMER') or hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(ApiResponse.error("User not authenticated"));
        }
        boolean isAdmin = userDetails.getUser().getRole() == com.croplink.entity.User.Role.ADMIN;
        productService.deleteProduct(id, userDetails.getId(), isAdmin);
        return ResponseEntity.ok(ApiResponse.success("Product deleted"));
    }

    @GetMapping("/admin/products")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> adminGetProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success("All products", productService.getAllProductsAdmin(page, size)));
    }

    @PatchMapping("/admin/products/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ProductResponse>> adminUpdateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                productService.adminUpdateStatus(id, body.get("status"))));
    }
}
