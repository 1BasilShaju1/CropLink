package service;

import model.Product;
import java.util.ArrayList;
import java.util.List;

public class ProductService {
    private final List<Product> products = new ArrayList<>();
    private int nextProductId = 1;

    public Product addProduct(Product product) {
        product.setId(nextProductId++);
        products.add(product);
        return product;
    }

    public List<Product> getAvailableProducts() {
        List<Product> availableList = new ArrayList<>();
        for (Product p : products) {
            if ("Available".equalsIgnoreCase(p.getAvailability()) || "Limited Stock".equalsIgnoreCase(p.getAvailability())) {
                availableList.add(p);
            }
        }
        return availableList;
    }

    public Product getProductById(int id) {
        for (Product p : products) {
            if (p.getId() == id) {
                return p;
            }
        }
        return null;
    }

    public List<Product> searchByName(String keyword) {
        List<Product> result = new ArrayList<>();
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAvailableProducts();
        }
        String lowerKey = keyword.toLowerCase().trim();
        for (Product p : getAvailableProducts()) {
            if (p.getProductName().toLowerCase().contains(lowerKey)) {
                result.add(p);
            }
        }
        return result;
    }

    public List<Product> filterByCategory(String category) {
        List<Product> result = new ArrayList<>();
        if (category == null || category.trim().isEmpty() || "All".equalsIgnoreCase(category)) {
            return getAvailableProducts();
        }
        for (Product p : getAvailableProducts()) {
            if (p.getProductCategory().equalsIgnoreCase(category.trim())) {
                result.add(p);
            }
        }
        return result;
    }

    public List<Product> getProductsByFarmerId(int farmerId) {
        List<Product> result = new ArrayList<>();
        for (Product p : products) {
            if (p.getFarmerId() == farmerId) {
                result.add(p);
            }
        }
        return result;
    }

    public boolean updateProduct(Product updatedProduct) {
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            if (p.getId() == updatedProduct.getId() && p.getFarmerId() == updatedProduct.getFarmerId()) {
                products.set(i, updatedProduct);
                return true;
            }
        }
        return false;
    }

    public boolean deleteProduct(int productId, int farmerId) {
        for (int i = 0; i < products.size(); i++) {
            Product p = products.get(i);
            if (p.getId() == productId && p.getFarmerId() == farmerId) {
                products.remove(i);
                return true;
            }
        }
        return false;
    }

    public boolean belongsToFarmer(int productId, int farmerId) {
        Product p = getProductById(productId);
        return p != null && p.getFarmerId() == farmerId;
    }

    public List<Product> getAllProducts() {
        return new ArrayList<>(products);
    }
}
