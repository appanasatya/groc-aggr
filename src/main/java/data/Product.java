package data;


public class Product {
    private String productId;
    private String superCategory;
    private String category;
    private String title;
    private String brand;
    private String volume;
    private Double minStorePrice;
    private Double maxStorePrice;

    public Product(String productId, String superCategory, String category, String title, String brand, String
            volume, Double minStorePrice,Double maxStorePrice) {
        this.productId = productId;
        this.superCategory = superCategory;
        this.category = category;
        this.title = title;
        this.brand = brand;
        this.volume = volume;
        this.minStorePrice = minStorePrice;
        this.maxStorePrice = maxStorePrice;
    }

    public Product() {

    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getSuperCategory() {
        return superCategory;
    }

    public void setSuperCategory(String superCategory) {
        this.superCategory = superCategory;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public Product(String productId) {
        this.productId = productId;
    }

    public Double getMinStorePrice() {
        return minStorePrice;
    }

    public void setMinStorePrice(Double minStorePrice) {
        this.minStorePrice = minStorePrice;
    }

    public Double getMaxStorePrice() {
        return maxStorePrice;
    }

    public void setMaxStorePrice(Double maxStorePrice) {
        this.maxStorePrice = maxStorePrice;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product product = (Product) o;

        if (brand != null ? !brand.equals(product.brand) : product.brand != null) return false;
        if (category != null ? !category.equals(product.category) : product.category != null) return false;
        if (maxStorePrice != null ? !maxStorePrice.equals(product.maxStorePrice) : product.maxStorePrice != null)
            return false;
        if (minStorePrice != null ? !minStorePrice.equals(product.minStorePrice) : product.minStorePrice != null)
            return false;
        if (productId != null ? !productId.equals(product.productId) : product.productId != null) return false;
        if (superCategory != null ? !superCategory.equals(product.superCategory) : product.superCategory != null)
            return false;
        if (title != null ? !title.equals(product.title) : product.title != null) return false;
        if (volume != null ? !volume.equals(product.volume) : product.volume != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = productId != null ? productId.hashCode() : 0;
        result = 31 * result + (superCategory != null ? superCategory.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (brand != null ? brand.hashCode() : 0);
        result = 31 * result + (volume != null ? volume.hashCode() : 0);
        result = 31 * result + (minStorePrice != null ? minStorePrice.hashCode() : 0);
        result = 31 * result + (maxStorePrice != null ? maxStorePrice.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", superCategory='" + superCategory + '\'' +
                ", category='" + category + '\'' +
                ", title='" + title + '\'' +
                ", brand='" + brand + '\'' +
                ", volume='" + volume + '\'' +
                ", minStorePrice=" + minStorePrice +
                ", maxStorePrice=" + maxStorePrice +
                '}';
    }
}
