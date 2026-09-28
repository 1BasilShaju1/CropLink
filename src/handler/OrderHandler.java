package handler;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import service.OrderService;
import service.ProductService;
import service.UserService;
import model.Order;
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

public class OrderHandler implements HttpHandler {
    private final OrderService orderService;
    private final ProductService productService;
    private final UserService userService;

    public OrderHandler(OrderService orderService, ProductService productService, UserService userService) {
        this.orderService = orderService;
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

        if ("POST".equalsIgnoreCase(method)) {
            handlePost(exchange);
        } else if ("GET".equalsIgnoreCase(method)) {
            handleGet(exchange, path, queryParams);
        } else if ("PUT".equalsIgnoreCase(method)) {
            handlePutStatus(exchange, queryParams);
        } else {
            ResponseUtil.sendJsonResponse(exchange, 405, JsonUtil.createResponse(false, "Method not allowed", null));
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        int buyerId = JsonUtil.getInt(body, "buyerId");
        int productId = JsonUtil.getInt(body, "productId");
        int quantity = JsonUtil.getInt(body, "quantity");

        User buyer = userService.findById(buyerId);
        if (buyer == null || !"BUYER".equalsIgnoreCase(buyer.getRole())) {
            ResponseUtil.sendJsonResponse(exchange, 403, JsonUtil.createResponse(false, "Only buyers can place orders.", null));
            return;
        }

        if (quantity <= 0) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Ordered quantity must be greater than zero.", null));
            return;
        }

        Product product = productService.getProductById(productId);
        if (product == null) {
            ResponseUtil.sendJsonResponse(exchange, 404, JsonUtil.createResponse(false, "Product not found.", null));
            return;
        }

        if (quantity > product.getQuantity()) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Ordered quantity cannot exceed available product quantity.", null));
            return;
        }

        Order order = orderService.placeOrder(buyerId, productId, quantity);
        if (order != null) {
            String orderJson = JsonUtil.orderToJson(order, product.getProductName());
            ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Order placed successfully.", orderJson));
        } else {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Failed to place order.", null));
        }
    }

    private void handleGet(HttpExchange exchange, String path, Map<String, String> queryParams) throws IOException {
        if (path.endsWith("/buyer")) {
            int buyerId = Integer.parseInt(queryParams.getOrDefault("buyerId", "0"));
            List<Order> buyerOrders = orderService.getOrdersByBuyerId(buyerId);
            String jsonArray = JsonUtil.orderListToJson(buyerOrders, productService);
            ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Buyer orders retrieved", jsonArray));
        } else if (path.endsWith("/farmer")) {
            int farmerId = Integer.parseInt(queryParams.getOrDefault("farmerId", "0"));
            List<Order> farmerOrders = orderService.getOrdersByFarmerId(farmerId);
            String jsonArray = JsonUtil.orderListToJson(farmerOrders, productService);
            ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Farmer orders retrieved", jsonArray));
        } else {
            ResponseUtil.sendJsonResponse(exchange, 404, JsonUtil.createResponse(false, "Endpoint not found", null));
        }
    }

    private void handlePutStatus(HttpExchange exchange, Map<String, String> queryParams) throws IOException {
        InputStream is = exchange.getRequestBody();
        String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);

        int orderId = Integer.parseInt(queryParams.getOrDefault("orderId", "0"));
        int farmerId = Integer.parseInt(queryParams.getOrDefault("farmerId", "0"));
        String newStatus = JsonUtil.getString(body, "orderStatus");

        if (orderId <= 0 || farmerId <= 0 || newStatus.isEmpty()) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Missing required parameters.", null));
            return;
        }

        if (!newStatus.equals("Pending") && !newStatus.equals("Accepted") && 
            !newStatus.equals("Rejected") && !newStatus.equals("Completed")) {
            ResponseUtil.sendJsonResponse(exchange, 400, JsonUtil.createResponse(false, "Invalid order status value.", null));
            return;
        }

        boolean updated = orderService.updateOrderStatus(orderId, farmerId, newStatus);
        if (updated) {
            Order o = orderService.getOrderById(orderId);
            Product p = (o != null) ? productService.getProductById(o.getProductId()) : null;
            String pName = (p != null) ? p.getProductName() : "Product";
            ResponseUtil.sendJsonResponse(exchange, 200, JsonUtil.createResponse(true, "Order status updated successfully.", JsonUtil.orderToJson(o, pName)));
        } else {
            ResponseUtil.sendJsonResponse(exchange, 403, JsonUtil.createResponse(false, "Unauthorized or order not found.", null));
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
