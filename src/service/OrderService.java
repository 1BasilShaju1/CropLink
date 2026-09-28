package service;

import model.Order;
import model.Product;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class OrderService {
    private final List<Order> orders = new ArrayList<>();
    private int nextOrderId = 1;
    private final ProductService productService;

    public OrderService(ProductService productService) {
        this.productService = productService;
    }

    public Order placeOrder(int buyerId, int productId, int quantity) {
        Product product = productService.getProductById(productId);
        if (product == null) {
            return null;
        }

        if (quantity <= 0 || quantity > product.getQuantity()) {
            return null;
        }

        double totalAmount = product.getPrice() * quantity;

        // Reduce product quantity
        int newQuantity = product.getQuantity() - quantity;
        product.setQuantity(newQuantity);

        // Update product availability status
        if (newQuantity == 0) {
            product.setAvailability("Out of Stock");
        } else if (newQuantity <= 5) {
            product.setAvailability("Limited Stock");
        }

        String orderDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        Order order = new Order(nextOrderId++, buyerId, productId, quantity, totalAmount, "Pending", orderDate);
        orders.add(order);
        return order;
    }

    public List<Order> getOrdersByBuyerId(int buyerId) {
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            if (o.getBuyerId() == buyerId) {
                result.add(o);
            }
        }
        return result;
    }

    public List<Order> getOrdersByFarmerId(int farmerId) {
        List<Order> result = new ArrayList<>();
        for (Order o : orders) {
            Product p = productService.getProductById(o.getProductId());
            if (p != null && p.getFarmerId() == farmerId) {
                result.add(o);
            }
        }
        return result;
    }

    public boolean updateOrderStatus(int orderId, int farmerId, String newStatus) {
        for (Order o : orders) {
            if (o.getId() == orderId) {
                Product p = productService.getProductById(o.getProductId());
                if (p != null && p.getFarmerId() == farmerId) {
                    o.setOrderStatus(newStatus);
                    return true;
                }
            }
        }
        return false;
    }

    public Order getOrderById(int orderId) {
        for (Order o : orders) {
            if (o.getId() == orderId) {
                return o;
            }
        }
        return null;
    }
}
