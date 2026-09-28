package model;

public class Product {
    private int id;
    private int farmerId;
    private String productName;
    private String productCategory;
    private double price;
    private int quantity;
    private String unit;
    private String quality;
    private String availability;
    private String deliveryOption;
    private String paymentMethod;
    private String description;

    public Product() {}

    public Product(int id, int farmerId, String productName, String productCategory, double price, int quantity, 
                   String unit, String quality, String availability, String deliveryOption, String paymentMethod, String description) {
        this.id = id;
        this.farmerId = farmerId;
        this.productName = productName;
        this.productCategory = productCategory;
        this.price = price;
        this.quantity = quantity;
        this.unit = unit;
        this.quality = quality;
        this.availability = availability;
        this.deliveryOption = deliveryOption;
        this.paymentMethod = paymentMethod;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFarmerId() { return farmerId; }
    public void setFarmerId(int farmerId) { this.farmerId = farmerId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getProductCategory() { return productCategory; }
    public void setProductCategory(String productCategory) { this.productCategory = productCategory; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getQuality() { return quality; }
    public void setQuality(String quality) { this.quality = quality; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getDeliveryOption() { return deliveryOption; }
    public void setDeliveryOption(String deliveryOption) { this.deliveryOption = deliveryOption; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", productName='" + productName + "', category='" + productCategory + "', price=" + price + "}";
    }
}
