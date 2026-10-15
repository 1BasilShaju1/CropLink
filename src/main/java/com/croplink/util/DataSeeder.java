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
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final ProductImageRepository productImageRepository;
    private final CartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        log.info("Verifying and seeding initial data if needed...");

        // 1. Admin
        User admin = userRepository.findByEmail("admin@croplink.com").orElse(null);
        if (admin == null) {
            admin = User.builder().fullName("Super Admin").email("admin@croplink.com")
                    .password(passwordEncoder.encode("Admin@123")).phone("9000000000")
                    .role(User.Role.ADMIN).status(User.UserStatus.ACTIVE).build();
            admin = userRepository.save(admin);
            log.info("Seeded Admin user.");
        }

        // 2. Farmer
        User farmer = userRepository.findByEmail("farmer@croplink.com").orElse(null);
        if (farmer == null) {
            farmer = User.builder().fullName("Rajan Kumar").email("farmer@croplink.com")
                    .password(passwordEncoder.encode("Farmer@123")).phone("9111111111")
                    .role(User.Role.FARMER).status(User.UserStatus.ACTIVE).build();
            farmer = userRepository.save(farmer);
            FarmerProfile fp = FarmerProfile.builder().user(farmer).farmName("Green Valley Farms")
                    .address("12, Farm Road").village("Kalyan").district("Thane")
                    .state("Maharashtra").pincode("421301").build();
            farmerProfileRepository.save(fp);
            log.info("Seeded Farmer user and profile.");
        } else if (farmerProfileRepository.findByUserId(farmer.getId()).isEmpty()) {
            FarmerProfile fp = FarmerProfile.builder().user(farmer).farmName("Green Valley Farms")
                    .address("12, Farm Road").village("Kalyan").district("Thane")
                    .state("Maharashtra").pincode("421301").build();
            farmerProfileRepository.save(fp);
        }

        // 3. Customer
        User customer = userRepository.findByEmail("customer@croplink.com").orElse(null);
        if (customer == null) {
            customer = User.builder().fullName("Priya Sharma").email("customer@croplink.com")
                    .password(passwordEncoder.encode("Customer@123")).phone("9222222222")
                    .role(User.Role.CUSTOMER).status(User.UserStatus.ACTIVE).build();
            customer = userRepository.save(customer);
            CustomerProfile cp = CustomerProfile.builder().user(customer).address("45, Main St")
                    .district("Mumbai").state("Maharashtra").pincode("400001").build();
            customerProfileRepository.save(cp);
            Cart cart = new Cart();
            cart.setCustomer(customer);
            cartRepository.save(cart);
            log.info("Seeded Customer user, profile, and cart.");
        }

        // 4. Delivery Partner
        User dp = userRepository.findByEmail("delivery@croplink.com").orElse(null);
        if (dp == null) {
            dp = User.builder().fullName("Suresh Delivery").email("delivery@croplink.com")
                    .password(passwordEncoder.encode("Delivery@123")).phone("9333333333")
                    .role(User.Role.DELIVERY_PARTNER).status(User.UserStatus.ACTIVE).build();
            dp = userRepository.save(dp);
            DeliveryPartnerProfile dpp = DeliveryPartnerProfile.builder().user(dp)
                    .vehicleType("Two Wheeler").serviceArea("Mumbai, Thane").status("AVAILABLE").build();
            deliveryPartnerProfileRepository.save(dpp);
            log.info("Seeded Delivery Partner user and profile.");
        }

        // 5. Categories
        if (categoryRepository.count() == 0) {
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
            for (String[] c : cats) {
                Category cat = Category.builder().name(c[0]).description(c[1]).status("ACTIVE").build();
                categoryRepository.save(cat);
            }
            log.info("Seeded default categories.");
        }

        // 6. Products
        if (productRepository.count() == 0 && farmer != null) {
            Category veggiesCat = categoryRepository.findByName("Vegetables").orElse(null);
            Category fruitsCat = categoryRepository.findByName("Fruits").orElse(veggiesCat);

            if (veggiesCat != null) {
                List<Object[]> products = List.of(
                    new Object[]{"Fresh Tomatoes", veggiesCat, "Juicy red tomatoes from the farm", new BigDecimal("30"), 100, "kg", "Grade A (Export / Prime)", "Ready to Eat", "Harvested Today", "100% Certified Organic", "Kalyan, Maharashtra", "https://images.unsplash.com/photo-1592924357228-91a4daadcfea?w=600"},
                    new Object[]{"Green Spinach", veggiesCat, "Tender spinach leaves, rich in iron", new BigDecimal("25"), 50, "kg", "Grade A (Export / Prime)", "Harvest Fresh", "Harvested Today", "Naturally Grown / Pesticide Free", "Kalyan, Maharashtra", "https://images.unsplash.com/photo-1576045057995-568f588f82fb?w=600"},
                    new Object[]{"Alphonso Mangoes", fruitsCat, "Premium Alphonso mangoes from Ratnagiri", new BigDecimal("350"), 30, "dozen", "Grade A (Export / Prime)", "Ready to Eat", "Harvested Today", "100% Certified Organic", "Ratnagiri, Maharashtra", "https://images.unsplash.com/photo-1553279768-865429fa0078?w=600"},
                    new Object[]{"Fresh Carrots", veggiesCat, "Crunchy orange carrots, farm fresh", new BigDecimal("40"), 80, "kg", "Grade B (Premium Market)", "Harvest Fresh", "Harvested Today", "Conventional Integrated", "Kalyan, Maharashtra", "https://images.unsplash.com/photo-1598170845058-32b9d6a5da37?w=600"},
                    new Object[]{"Baby Potatoes", veggiesCat, "Small creamy baby potatoes, ideal for curries", new BigDecimal("35"), 200, "kg", "Grade B (Premium Market)", "Harvest Fresh", "Harvested Today", "Conventional Integrated", "Kalyan, Maharashtra", "https://images.unsplash.com/photo-1518977676601-b53f82aba655?w=600"}
                );
                for (Object[] p : products) {
                    Product product = Product.builder()
                            .farmer(farmer).category((Category) p[1]).name((String) p[0])
                            .description((String) p[2]).price((BigDecimal) p[3])
                            .quantityAvailable((Integer) p[4]).unit((String) p[5])
                            .qualityGrade((String) p[6]).ripenessLevel((String) p[7])
                            .harvestDate(LocalDate.now())
                            .freshnessStatus((String) p[8]).organicStatus((String) p[9])
                            .location((String) p[10]).deliveryOptions("Farmer Delivery, Farm Pickup")
                            .paymentOptions("Cash on Delivery, UPI")
                            .minimumOrderQuantity(1).availability("Available")
                            .productStatus("ACTIVE").averageRating(4.8).totalReviews(12)
                            .build();
                    product = productRepository.save(product);
                    ProductImage pi = ProductImage.builder().product(product).imageUrl((String) p[11]).isPrimary(true).build();
                    productImageRepository.save(pi);
                }
                log.info("Seeded default active products.");
            }
        }

        // 7. Seed Sample Orders if empty
        if (orderRepository.count() == 0 && customer != null && farmer != null) {
            List<Product> prods = productRepository.findByFarmerId(farmer.getId());
            if (!prods.isEmpty()) {
                Product p1 = prods.get(0);
                Product p2 = prods.size() > 1 ? prods.get(1) : p1;
                Product p3 = prods.size() > 2 ? prods.get(2) : p1;
                Product p4 = prods.size() > 3 ? prods.get(3) : p1;

                // Order 1: PENDING (For Farmer to Accept/Reject)
                BigDecimal ord1ItemsTotal = p1.getPrice().multiply(new BigDecimal("2"));
                Order ord1 = Order.builder()
                        .customer(customer).farmer(farmer)
                        .totalAmount(ord1ItemsTotal).deliveryFee(new BigDecimal("40"))
                        .finalAmount(ord1ItemsTotal.add(new BigDecimal("40")))
                        .deliveryMethod("Doorstep Delivery").paymentMethod("Cash on Delivery")
                        .paymentStatus("PENDING").deliveryAddress("45, Main St, Mumbai - 400001")
                        .orderStatus("PENDING").orderDate(LocalDateTime.now().minusHours(2))
                        .build();
                ord1 = orderRepository.save(ord1);
                OrderItem item1 = OrderItem.builder().order(ord1).product(p1).productName(p1.getName())
                        .quantity(2).unitPrice(p1.getPrice()).subtotal(ord1ItemsTotal).build();
                orderItemRepository.save(item1);
                Payment pay1 = Payment.builder().order(ord1).paymentMethod("Cash on Delivery")
                        .paymentStatus("PENDING").amount(ord1.getFinalAmount()).build();
                paymentRepository.save(pay1);

                // Order 2: PACKED (Ready for Delivery Partner to Accept)
                BigDecimal ord2ItemsTotal = p2.getPrice().multiply(new BigDecimal("3"));
                Order ord2 = Order.builder()
                        .customer(customer).farmer(farmer)
                        .totalAmount(ord2ItemsTotal).deliveryFee(new BigDecimal("40"))
                        .finalAmount(ord2ItemsTotal.add(new BigDecimal("40")))
                        .deliveryMethod("Doorstep Delivery").paymentMethod("UPI")
                        .paymentStatus("PAID").deliveryAddress("45, Main St, Mumbai - 400001")
                        .orderStatus("PACKED").orderDate(LocalDateTime.now().minusHours(5))
                        .build();
                ord2 = orderRepository.save(ord2);
                OrderItem item2 = OrderItem.builder().order(ord2).product(p2).productName(p2.getName())
                        .quantity(3).unitPrice(p2.getPrice()).subtotal(ord2ItemsTotal).build();
                orderItemRepository.save(item2);
                Payment pay2 = Payment.builder().order(ord2).paymentMethod("UPI")
                        .paymentStatus("PAID").amount(ord2.getFinalAmount()).build();
                paymentRepository.save(pay2);

                // Order 3: OUT_FOR_DELIVERY (Assigned to Suresh Delivery)
                BigDecimal ord3ItemsTotal = p3.getPrice().multiply(new BigDecimal("1"));
                Order ord3 = Order.builder()
                        .customer(customer).farmer(farmer).deliveryPartner(dp)
                        .totalAmount(ord3ItemsTotal).deliveryFee(new BigDecimal("40"))
                        .finalAmount(ord3ItemsTotal.add(new BigDecimal("40")))
                        .deliveryMethod("Doorstep Delivery").paymentMethod("UPI")
                        .paymentStatus("PAID").deliveryAddress("45, Main St, Mumbai - 400001")
                        .orderStatus("OUT_FOR_DELIVERY").orderDate(LocalDateTime.now().minusHours(8))
                        .build();
                ord3 = orderRepository.save(ord3);
                OrderItem item3 = OrderItem.builder().order(ord3).product(p3).productName(p3.getName())
                        .quantity(1).unitPrice(p3.getPrice()).subtotal(ord3ItemsTotal).build();
                orderItemRepository.save(item3);
                Payment pay3 = Payment.builder().order(ord3).paymentMethod("UPI")
                        .paymentStatus("PAID").amount(ord3.getFinalAmount()).build();
                paymentRepository.save(pay3);

                // Order 4: DELIVERED (Delivered revenue, reviewable)
                BigDecimal ord4ItemsTotal = p4.getPrice().multiply(new BigDecimal("4"));
                Order ord4 = Order.builder()
                        .customer(customer).farmer(farmer).deliveryPartner(dp)
                        .totalAmount(ord4ItemsTotal).deliveryFee(new BigDecimal("40"))
                        .finalAmount(ord4ItemsTotal.add(new BigDecimal("40")))
                        .deliveryMethod("Doorstep Delivery").paymentMethod("Cash on Delivery")
                        .paymentStatus("PAID").deliveryAddress("45, Main St, Mumbai - 400001")
                        .orderStatus("DELIVERED").orderDate(LocalDateTime.now().minusDays(1))
                        .deliveredDate(LocalDateTime.now().minusHours(4))
                        .build();
                ord4 = orderRepository.save(ord4);
                OrderItem item4 = OrderItem.builder().order(ord4).product(p4).productName(p4.getName())
                        .quantity(4).unitPrice(p4.getPrice()).subtotal(ord4ItemsTotal).build();
                orderItemRepository.save(item4);
                Payment pay4 = Payment.builder().order(ord4).paymentMethod("Cash on Delivery")
                        .paymentStatus("PAID").amount(ord4.getFinalAmount()).build();
                paymentRepository.save(pay4);

                // Seed review for product 4
                Review rev = Review.builder().customer(customer).product(p4).order(ord4)
                        .rating(5).comment("Exceptional quality and freshness! Fast delivery.")
                        .createdAt(LocalDateTime.now().minusHours(3))
                        .build();
                reviewRepository.save(rev);

                log.info("Seeded initial realistic orders and review.");
            }
        }

        log.info("Data verification and seeding complete!");
    }
}
