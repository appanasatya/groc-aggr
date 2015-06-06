package data;

import java.util.List;

public class SurpriseListOfStores {
    public List<StoreProductList> storeProductLists;
    public Double totalSurpriseAmount;

    public SurpriseListOfStores(List<StoreProductList> storeProductLists,Double totalSurpriseAmount) {
        this.storeProductLists = storeProductLists;
        this.totalSurpriseAmount = totalSurpriseAmount;
    }

    public SurpriseListOfStores() {
    }
}
