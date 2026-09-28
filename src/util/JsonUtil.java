package util;

import model.User;
import model.Product;
import model.Order;
import service.ProductService;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonUtil {

    public static String getString(String json, String key) {
        if (json == null) return "";
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            return unescape(matcher.group(1));
        }
        return "";
    }

    public static int getInt(String json, String key) {
        if (json == null) return 0;
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public static double getDouble(String json, String key) {
        if (json == null) return 0.0;
        Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*(-?\\d+(\\.\\d+)?)");
        Matcher matcher = pattern.matcher(json);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public static String escape(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\b", "\\b")
                    .replace("\f", "\\f")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private static String unescape(String input) {
        if (input == null) return "";
        return input.replace("\\\"", "\"")
                    .replace("\\\\", "\\")
                    .replace("\\n", "\n")
                    .replace("\\r", "\r")
                    .replace("\\t", "\t");
    }

    public static String userToJson(User user) {
        if (user == null) return "null";
        return String.format(
            "{\"id\":%d,\"name\":\"%s\",\"email\":\"%s\",\"role\":\"%s\"}",
            user.getId(),
            escape(user.getName()),
            escape(user.getEmail()),
            escape(user.getRole())
        );
    }

    public static String productToJson(Product p) {
        if (p == null) return "null";
        return String.format(
            "{\"id\":%d,\"farmerId\":%d,\"productName\":\"%s\",\"productCategory\":\"%s\",\"price\":%.2f,\"quantity\":%d,\"unit\":\"%s\",\"quality\":\"%s\",\"availability\":\"%s\",\"deliveryOption\":\"%s\",\"paymentMethod\":\"%s\",\"description\":\"%s\"}",
            p.getId(),
            p.getFarmerId(),
            escape(p.getProductName()),
            escape(p.getProductCategory()),
            p.getPrice(),
            p.getQuantity(),
            escape(p.getUnit()),
            escape(p.getQuality()),
            escape(p.getAvailability()),
            escape(p.getDeliveryOption()),
            escape(p.getPaymentMethod()),
            escape(p.getDescription())
        );
    }

    public static String productListToJson(List<Product> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append(productToJson(list.get(i)));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String orderToJson(Order o, String productName) {
        if (o == null) return "null";
        return String.format(
            "{\"id\":%d,\"buyerId\":%d,\"productId\":%d,\"productName\":\"%s\",\"quantity\":%d,\"totalAmount\":%.2f,\"orderStatus\":\"%s\",\"orderDate\":\"%s\"}",
            o.getId(),
            o.getBuyerId(),
            o.getProductId(),
            escape(productName != null ? productName : "Unknown Product"),
            o.getQuantity(),
            o.getTotalAmount(),
            escape(o.getOrderStatus()),
            escape(o.getOrderDate())
        );
    }

    public static String orderListToJson(List<Order> list, ProductService productService) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            Order o = list.get(i);
            Product p = productService.getProductById(o.getProductId());
            String pName = (p != null) ? p.getProductName() : "Product #" + o.getProductId();
            sb.append(orderToJson(o, pName));
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    public static String createResponse(boolean success, String message, String dataJson) {
        StringBuilder sb = new StringBuilder("{");
        sb.append("\"success\":").append(success).append(",");
        sb.append("\"message\":\"").append(escape(message)).append("\"");
        if (dataJson != null && !dataJson.isEmpty()) {
            sb.append(",\"data\":").append(dataJson);
        }
        sb.append("}");
        return sb.toString();
    }
}
