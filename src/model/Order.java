package model;

public class Order {
    private int id;
    private int buyerId;
    private int productId;
    private int quantity;
    private double totalAmount;
    private String orderStatus; // Pending, Accepted, Rejected, Completed
    private String orderDate;

    public Order() {}

    public Order(int id, int buyerId, int productId, int quantity, double totalAmount, String orderStatus, String orderDate) {
        this.id = id;
        this.buyerId = buyerId;
        this.productId = productId;
        this.quantity = quantity;
        this.totalAmount = totalAmount;
        this.orderStatus = orderStatus;
        this.orderDate = orderDate;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getOrderDate() { return orderDate; }
    public void setOrderDate(String orderDate) { this.orderDate = orderDate; }

    @Override
    public String toString() {
        return "Order{id=" + id + ", buyerId=" + buyerId + ", productId=" + productId + ", status='" + orderStatus + "'}";
    }
}
