package com.croplink.util;

import com.croplink.entity.*;
import com.croplink.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder {
    private final UserRepository userRepository;
    private final FarmerProfileRepository farmerProfileRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final DeliveryPartnerProfileRepository deliveryPartnerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (userRepository.existsByEmail("admin@croplink.com")) {
            log.info("Data already seeded, skipping.");
            return;
        }
        log.info("Seeding initial data...");

        // Admin
        User admin = User.builder().fullName("Super Admin").email("admin@croplink.com")
                .password(passwordEncoder.encode("Admin@123")).phone("9000000000")
                .role(User.Role.ADMIN).status(User.UserStatus.ACTIVE).build();
        userRepository.save(admin);

        // Farmer
        User farmer = User.builder().fullName("Rajan Kumar").email("farmer@croplink.com")
                .password(passwordEncoder.encode("Farmer@123")).phone("9111111111")
                .role(User.Role.FARMER).status(User.UserStatus.ACTIVE).build();
        farmer = userRepository.save(farmer);
        FarmerProfile fp = FarmerProfile.builder().user(farmer).farmName("Green Valley Farms")
                .address("12, Farm Road").village("Kalyan").district("Thane")
                .state("Maharashtra").pincode("421301").build();
        farmerProfileRepository.save(fp);

        // Customer
        User customer = User.builder().fullName("Priya Sharma").email("customer@croplink.com")
                .password(passwordEncoder.encode("Customer@123")).phone("9222222222")
                .role(User.Role.CUSTOMER).status(User.UserStatus.ACTIVE).build();
        customer = userRepository.save(customer);
        CustomerProfile cp = CustomerProfile.builder().user(customer).address("45, Main St")
                .district("Mumbai").state("Maharashtra").pincode("400001").build();
        customerProfileRepository.save(cp);
        Cart cart = new Cart(); cart.setCustomer(customer);
        cartRepository.save(cart);

        // Delivery Partner
        User dp = User.builder().fullName("Suresh Delivery").email("delivery@croplink.com")
                .password(passwordEncoder.encode("Delivery@123")).phone("9333333333")
                .role(User.Role.DELIVERY_PARTNER).status(User.UserStatus.ACTIVE).build();
        dp = userRepository.save(dp);
        DeliveryPartnerProfile dpp = DeliveryPartnerProfile.builder().user(dp)
                .vehicleType("Two Wheeler").serviceArea("Mumbai, Thane").status("AVAILABLE").build();
        deliveryPartnerProfileRepository.save(dpp);

        // Categories
        List<String[]> cats = List.of(
            new String[]{"Vegetables", "Fresh farm vegetables"},
            new String[]{"Fruits", "Seasonal fresh fruits"},
            new String[]{"Grains", "Rice, wheat and cereals"},
            new String[]{"Spices", "Aromatic Indian spices"},
            new String[]{"Pulses", "Lentils, beans and dals"},
            new String[]{"Dairy Products", "Fresh milk and dairy"},
            new String[]{"Flowers", "Fresh cut flowers"},
            new String[]{"Seeds And Plants", "Seeds and saplings"},
            new String[]{"Other Products", "Miscellaneous farm products"}
        );
        Category veggiesCat = null;
        for (String[] c : cats) {
            Category cat = Category.builder().name(c[0]).description(c[1]).status("ACTIVE").build();
            cat = categoryRepository.save(cat);
            if (c[0].equals("Vegetables")) veggiesCat = cat;
        }

        // Products
        if (veggiesCat != null) {
            Category fruitsCat = categoryRepository.findByName("Fruits").orElse(veggiesCat);
            List<Object[]> products = List.of(
                new Object[]{"Fresh Tomatoes", veggiesCat, "Juicy red tomatoes from the farm", new BigDecimal("30"), 100, "kg", "Premium", "Ripe", "Harvested Today", "Organic", "Kalyan, Maharashtra"},
                new Object[]{"Green Spinach", veggiesCat, "Tender spinach leaves, rich in iron", new BigDecimal("25"), 50, "kg", "Premium", "Semi-Ripe", "Harvested Today", "Pesticide-Free", "Kalyan, Maharashtra"},
                new Object[]{"Alphonso Mangoes", fruitsCat, "Premium Alphonso mangoes from Ratnagiri", new BigDecimal("350"), 30, "dozen", "Premium", "Ripe", "1 Day Old", "Organic", "Ratnagiri, Maharashtra"},
                new Object[]{"Fresh Carrots", veggiesCat, "Crunchy orange carrots, farm fresh", new BigDecimal("40"), 80, "kg", "Standard", "Fully Ripe", "Harvested Today", "Conventional", "Kalyan, Maharashtra"},
                new Object[]{"Baby Potatoes", veggiesCat, "Small creamy baby potatoes, ideal for curries", new BigDecimal("35"), 200, "kg", "Standard", "Fully Ripe", "1 Day Old", "Conventional", "Kalyan, Maharashtra"}
            );
            for (Object[] p : products) {
                Product product = Product.builder()
                        .farmer(farmer).category((Category) p[1]).name((String) p[0])
                        .description((String) p[2]).price((BigDecimal) p[3])
                        .quantityAvailable((Integer) p[4]).unit((String) p[5])
                        .qualityGrade((String) p[6]).ripenessLevel((String) p[7])
                        .harvestDate(LocalDate.now())
                        .freshnessStatus((String) p[8]).organicStatus((String) p[9])
                        .location((String) p[10]).deliveryOptions("Farmer Delivery,Farm Pickup")
                        .paymentOptions("Cash on Delivery,UPI")
                        .minimumOrderQuantity(1).availability("Available")
                        .productStatus("ACTIVE").averageRating(4.5).totalReviews(10)
                        .build();
                productRepository.save(product);
            }
        }
        log.info("Data seeding complete!");
    }
}
