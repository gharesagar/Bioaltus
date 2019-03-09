package model;

public class Product {
    private Integer productId;
    private String productName;

    public Product(Integer productId, String productName) {
        this.productId = productId;
        this.productName = productName;
    }

    public Integer getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }
}
