package data;

import com.google.common.collect.Lists;

import java.util.List;

public class StoreProductList {
    public String storeName;
    public List<String> productList;
    public Double totalAmount = 0.0;
    public Double savedAmount = 0.0;

    public StoreProductList(String storeName) {
        this.storeName = storeName;
    }

    public StoreProductList(String storeName,Double savedAmount) {
        this.storeName = storeName;
        this.savedAmount = savedAmount;
    }

    public StoreProductList() {
    }

    public void addProduct(String productName) {
        if (productList == null)
            productList = Lists.newArrayList();
        productList.add(productName);
    }
    
    public void addToTotal(Double amt) {
        totalAmount+= amt;
    }

    public void addToSaving(Double savedAmt) {
        savedAmount+= savedAmt;
    }

}
