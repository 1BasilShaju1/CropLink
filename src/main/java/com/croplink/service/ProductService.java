package com.croplink.service;

import com.croplink.dto.ProductRequest;
import com.croplink.dto.ProductResponse;
import com.croplink.entity.*;
import com.croplink.exception.AppException;
import com.croplink.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FarmerProfileRepository farmerProfileRepository;
    private final ProductImageRepository productImageRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public ProductResponse createProduct(ProductRequest request, List<MultipartFile> images, Long farmerId) {
        User farmer = userRepository.findById(farmerId).orElseThrow(() -> new AppException("Farmer not found", 404));
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException("Category not found", 404));
        Product product = Product.builder()
                .farmer(farmer)
                .category(category)
                .name(toTitleCase(request.getName()))
                .description(request.getDescription())
                .price(request.getPrice())
                .quantityAvailable(request.getQuantityAvailable())
                .unit(request.getUnit())
                .qualityGrade(request.getQualityGrade())
                .ripenessLevel(request.getRipenessLevel())
                .harvestDate(request.getHarvestDate())
                .freshnessStatus(request.getFreshnessStatus())
                .organicStatus(request.getOrganicStatus())
                .location(request.getLocation())
                .deliveryOptions(request.getDeliveryOptions())
                .paymentOptions(request.getPaymentOptions())
                .minimumOrderQuantity(request.getMinimumOrderQuantity() != null ? request.getMinimumOrderQuantity() : 1)
                .availability(request.getAvailability() != null ? request.getAvailability() : "Available")
                .productStatus("PENDING_APPROVAL")
                .averageRating(0.0)
                .totalReviews(0)
                .build();
        product = productRepository.save(product);
        if (images != null && !images.isEmpty()) {
            boolean first = true;
            for (MultipartFile img : images) {
                if (!img.isEmpty()) {
                    String url = fileStorageService.storeFile(img);
                    ProductImage pi = ProductImage.builder().product(product).imageUrl(url).isPrimary(first).build();
                    productImageRepository.save(pi);
                    first = false;
                }
            }
        }
        return toResponse(product);
    }

    public Page<ProductResponse> getActiveProducts(Long categoryId, String search, int page, int size, String sort) {
        Sort sorting = sort != null && sort.equals("price_asc") ? Sort.by("price").ascending() :
                       sort != null && sort.equals("price_desc") ? Sort.by("price").descending() :
                       sort != null && sort.equals("rating") ? Sort.by("averageRating").descending() :
                       Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sorting);
        return productRepository.findActiveProducts(categoryId, search, pageable).map(this::toResponse);
    }

    public Page<ProductResponse> getFarmerProducts(Long farmerId, String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        if (status != null && !status.isBlank()) {
            return productRepository.findByFarmerIdAndProductStatus(farmerId, status, pageable).map(this::toResponse);
        }
        return productRepository.findByFarmerId(farmerId, pageable).map(this::toResponse);
    }

    public List<ProductResponse> getFarmerProductsList(Long farmerId) {
        return productRepository.findByFarmerId(farmerId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product p = productRepository.findById(id).orElseThrow(() -> new AppException("Product not found", 404));
        return toResponse(p);
    }

    @Transactional
    public ProductResponse updateProduct(Long productId, ProductRequest request, List<MultipartFile> images, Long farmerId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException("Product not found", 404));
        if (!product.getFarmer().getId().equals(farmerId)) throw new AppException("Not authorized", 403);
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException("Category not found", 404));
        product.setName(toTitleCase(request.getName()));
        product.setCategory(category);
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantityAvailable(request.getQuantityAvailable());
        product.setUnit(request.getUnit());
        product.setQualityGrade(request.getQualityGrade());
        product.setRipenessLevel(request.getRipenessLevel());
        product.setHarvestDate(request.getHarvestDate());
        product.setFreshnessStatus(request.getFreshnessStatus());
        product.setOrganicStatus(request.getOrganicStatus());
        product.setLocation(request.getLocation());
        product.setDeliveryOptions(request.getDeliveryOptions());
        product.setPaymentOptions(request.getPaymentOptions());
        if (request.getMinimumOrderQuantity() != null) product.setMinimumOrderQuantity(request.getMinimumOrderQuantity());
        if (request.getAvailability() != null) product.setAvailability(request.getAvailability());
        if (images != null && !images.isEmpty()) {
            List<ProductImage> existing = productImageRepository.findByProductId(productId);
            existing.forEach(img -> fileStorageService.deleteFile(img.getImageUrl()));
            productImageRepository.deleteByProductId(productId);
            boolean first = true;
            for (MultipartFile img : images) {
                if (!img.isEmpty()) {
                    String url = fileStorageService.storeFile(img);
                    ProductImage pi = ProductImage.builder().product(product).imageUrl(url).isPrimary(first).build();
                    productImageRepository.save(pi);
                    first = false;
                }
            }
        }
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long productId, Long farmerId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new AppException("Product not found", 404));
        if (!product.getFarmer().getId().equals(farmerId)) throw new AppException("Not authorized", 403);
        List<ProductImage> images = productImageRepository.findByProductId(productId);
        images.forEach(img -> fileStorageService.deleteFile(img.getImageUrl()));
        productRepository.delete(product);
    }

    public Page<ProductResponse> getAllProductsAdmin(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending())).map(this::toResponse);
    }

    public ProductResponse adminUpdateStatus(Long id, String status) {
        Product p = productRepository.findById(id).orElseThrow(() -> new AppException("Product not found", 404));
        p.setProductStatus(status);
        return toResponse(productRepository.save(p));
    }

    private String toTitleCase(String input) {
        if (input == null || input.isBlank()) return input;
        String[] words = input.trim().toLowerCase().split("\\s+");
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public ProductResponse toResponse(Product p) {
        List<ProductImage> images = productImageRepository.findByProductId(p.getId());
        List<String> imageUrls = images.stream().map(ProductImage::getImageUrl).collect(Collectors.toList());
        String primaryImageUrl = images.stream().filter(ProductImage::getIsPrimary).map(ProductImage::getImageUrl).findFirst()
                .orElse(imageUrls.isEmpty() ? null : imageUrls.get(0));
        FarmerProfile fp = farmerProfileRepository.findByUserId(p.getFarmer().getId()).orElse(null);
        return ProductResponse.builder()
                .id(p.getId())
                .farmerId(p.getFarmer().getId())
                .farmerName(p.getFarmer().getFullName())
                .farmName(fp != null ? fp.getFarmName() : "")
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .quantityAvailable(p.getQuantityAvailable())
                .unit(p.getUnit())
                .qualityGrade(p.getQualityGrade())
                .ripenessLevel(p.getRipenessLevel())
                .harvestDate(p.getHarvestDate())
                .freshnessStatus(p.getFreshnessStatus())
                .organicStatus(p.getOrganicStatus())
                .location(p.getLocation())
                .deliveryOptions(p.getDeliveryOptions())
                .paymentOptions(p.getPaymentOptions())
                .minimumOrderQuantity(p.getMinimumOrderQuantity())
                .availability(p.getAvailability())
                .productStatus(p.getProductStatus())
                .averageRating(p.getAverageRating())
                .totalReviews(p.getTotalReviews())
                .imageUrls(imageUrls)
                .primaryImageUrl(primaryImageUrl)
                .createdAt(p.getCreatedAt())
                .build();
    }
}
