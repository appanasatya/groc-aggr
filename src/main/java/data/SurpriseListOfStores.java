package data;

import java.util.List;

public class SurpriseListOfStores {
    public List<StoreProductList> storeProductLists;
    public Double finalAmount;
    public Double totalSavedAmount;

    public SurpriseListOfStores(List<StoreProductList> storeProductLists,Double finalAmount,Double totalSavedAmount) {
        this.storeProductLists = storeProductLists;
        this.finalAmount = finalAmount;
        this.totalSavedAmount = totalSavedAmount;
    }

    public SurpriseListOfStores() {
    }
}
