import com.sun.net.httpserver.HttpServer;
import handler.AuthHandler;
import handler.OrderHandler;
import handler.ProductHandler;
import handler.StaticFileHandler;
import model.Product;
import model.User;
import service.OrderService;
import service.ProductService;
import service.UserService;

import java.io.IOException;
import java.net.InetSocketAddress;

public class Main {
    public static void main(String[] args) throws IOException {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Initialize Services
        UserService userService = new UserService();
        ProductService productService = new ProductService();
        OrderService orderService = new OrderService(productService);

        // Seed Sample Users
        // Sample Farmer: Ravi Farmer (farmer@test.com / 1234)
        User farmer = userService.registerUser("Ravi Farmer", "farmer@test.com", "1234", "FARMER");

        // Sample Buyer: Arun Buyer (buyer@test.com / 1234)
        User buyer = userService.registerUser("Arun Buyer", "buyer@test.com", "1234", "BUYER");

        // Seed Sample Products for Ravi Farmer (farmerId = 1)
        if (farmer != null) {
            // Product 1: Tomato
            productService.addProduct(new Product(
                0, farmer.getId(), "Tomato", "Vegetables", 40.0, 50, "kg",
                "Fresh", "Available", "Farm Pickup", "Cash on Delivery",
                "Fresh tomatoes from the local farm."
            ));

            // Product 2: Banana
            productService.addProduct(new Product(
                0, farmer.getId(), "Banana", "Fruits", 60.0, 20, "dozen",
                "Good", "Available", "Home Delivery", "UPI",
                "Fresh bananas available for sale."
            ));

            // Product 3: Black Pepper
            productService.addProduct(new Product(
                0, farmer.getId(), "Black Pepper", "Spices", 500.0, 10, "kg",
                "Fresh", "Limited Stock", "Farm Pickup", "Cash on Delivery",
                "High-quality black pepper."
            ));
        }

        // Register HTTP Endpoints
        server.createContext("/api/auth", new AuthHandler(userService));
        server.createContext("/api/products", new ProductHandler(productService, userService));
        server.createContext("/api/orders", new OrderHandler(orderService, productService, userService));
        server.createContext("/", new StaticFileHandler("public"));

        server.setExecutor(null);
        server.start();

        System.out.println("=================================================");
        System.out.println(" CropLink - Farmer Product Management System");
        System.out.println(" Server running on: http://localhost:" + port);
        System.out.println("=================================================");
    }
}
