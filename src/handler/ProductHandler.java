package handler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import service.ProductService;
import service.UserService;
import model.Product;
import model.User;
import util.JsonUtil;
import util.ResponseUtil;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductHandler implements HttpHandler {
    private final ProductService productService;
    private final UserService userService;

    public ProductHandler(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (ResponseUtil.handleCorsPreflight(exchange)) return;

        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String query = exchange.getRequestURI().getQuery();
        Map<String, String> queryParams = parseQuery(query);

        if ("GET".equalsIgnoreCase(method)) {
            handleGet(exchange, path, queryParams);
        } else if ("POST".equalsIgnoreCase(method)) {
            handlePost(exchange);
        } else if ("PUT".equalsIgnoreCase(method)) {
            handlePut(exchange, queryParams);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            handleDelete(exchange, queryParams);
        } else {
            ResponseUtil.sendJsonResponse(exchange, 405, JsonUtil.createResponse(false, "Method not allowed", null));
        }
    }

    private void handleGet(HttpExchange exchange, String path, Map<String, String> queryParams) throws IOException {
        if (path.endsWith("/search")) {
            String name = queryParams.getOrDefault("name", "");
            List<Product> list = productService.searchByName(name);
            ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Products found", JsonUtil.productListToJson(list)));
        } else if (path.endsWith("/category")) {
            String category = queryParams.getOrDefault("category", "");
            List<Product> list = productService.filterByCategory(category);
            ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Products found", JsonUtil.productListToJson(list)));
        } else if (path.endsWith("/farmer")) {
            int farmerId = Integer.parseInt(queryParams.getOrDefault("farmerId", "0"));
            List<Product> list = productService.getProductsByFarmerId(farmerId);
            ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Farmer products retrieved", JsonUtil.productListToJson(list)));
        } else if (queryParams.containsKey("id")) {
            int id = Integer.parseInt(queryParams.get("id"));
            Product product = productService.getProductById(id);
            if (product != null) {
                ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Product found", JsonUtil.productToJson(product)));
            } else {
                ResponseUtil.sendJsonResponse(exchange, 404, JsonUtil.createResponse(false, "Product not found", null));
            }
        } else {
            List<Product> list = productService.getAvailableProducts();
            ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Available products retrieved", JsonUtil.productListToJson(list)));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        int farmerId = JsonUtil.getInt(body, "farmerId");
        User farmer = userService.findById(farmerId);
        if (farmer == null || !"FARMER".equalsIgnoreCase(farmer.getRole())) {
            ResponseUtil.sendJsonResponse(exchange, 403, JsonUtil.createResponse(false, "Only farmers can add products.", null));
            return;
        }

        String name = JsonUtil.getString(body, "productName");
        String category = JsonUtil.getString(body, "productCategory");
        double price = JsonUtil.getDouble(body, "price");
        int quantity = JsonUtil.getInt(body, "quantity");
        String unit = JsonUtil.getString(body, "unit");
        String quality = JsonUtil.getString(body, "quality");
        String availability = JsonUtil.getString(body, "availability");
        String deliveryOption = JsonUtil.getString(body, "deliveryOption");
        String paymentMethod = JsonUtil.getString(body, "paymentMethod");
        String description = JsonUtil.getString(body, "description");

        if (name.isEmpty() || category.isEmpty() || unit.isEmpty() || quality.isEmpty() || 
            availability.isEmpty() || deliveryOption.isEmpty() || paymentMethod.isEmpty()) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Please fill in all required fields.", null));
            return;
        }

        if (price <= 0) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Product price must be greater than zero.", null));
            return;
        }

        if (quantity <= 0) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Product quantity must be greater than zero.", null));
            return;
        }

        Product product = new Product(0, farmerId, name, category, price, quantity, unit, quality, availability, deliveryOption, paymentMethod, description);
        productService.addProduct(product);
        ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Product added successfully.", JsonUtil.productToJson(product)));
    }

    private void handlePut(HttpExchange exchange, Map<String, String> queryParams) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        int productId = queryParams.containsKey("id") ? Integer.parseInt(queryParams.get("id")) : JsonUtil.getInt(body, "id");
        int farmerId = JsonUtil.getInt(body, "farmerId");

        if (productId <= 0 || farmerId <= 0) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Invalid product or farmer ID.", null));
            return;
        }

        if (!productService.belongsToFarmer(productId, farmerId)) {
            ResponseUtil.sendJsonResponse(exchange, 403, JsonUtil.createResponse(false, "Unauthorized to update this product.", null));
            return;
        }

        String name = JsonUtil.getString(body, "productName");
        String category = JsonUtil.getString(body, "productCategory");
        double price = JsonUtil.getDouble(body, "price");
        int quantity = JsonUtil.getInt(body, "quantity");
        String unit = JsonUtil.getString(body, "unit");
        String quality = JsonUtil.getString(body, "quality");
        String availability = JsonUtil.getString(body, "availability");
        String deliveryOption = JsonUtil.getString(body, "deliveryOption");
        String paymentMethod = JsonUtil.getString(body, "paymentMethod");
        String description = JsonUtil.getString(body, "description");

        if (price <= 0 || quantity <= 0) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Price and quantity must be greater than zero.", null));
            return;
        }

        Product updated = new Product(productId, farmerId, name, category, price, quantity, unit, quality, availability, deliveryOption, paymentMethod, description);
        boolean success = productService.updateProduct(updated);
        if (success) {
            ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Product updated successfully.", JsonUtil.productToJson(updated)));
        } else {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Failed to update product.", null));
        }
    }

    private void handleDelete(HttpExchange exchange, Map<String, String> queryParams) throws IOException {
        int id = Integer.parseInt(queryParams.getOrDefault("id", "0"));
        int farmerId = Integer.parseInt(queryParams.getOrDefault("farmerId", "0"));

        if (id <= 0 || farmerId <= 0) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Missing product or farmer ID.", null));
            return;
        }

        boolean deleted = productService.deleteProduct(id, farmerId);
        if (deleted) {
            ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Product deleted successfully.", null));
        } else {
            ResponseUtil.sendJsonResponse(exchange, 403, JsonUtil.createResponse(false, "Unauthorized or product not found.", null));
        }
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                map.put(kv[0], java.net.URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            } else if (kv.length == 1) {
                map.put(kv[0], "");
            }
        }
        return map;
    }
}
