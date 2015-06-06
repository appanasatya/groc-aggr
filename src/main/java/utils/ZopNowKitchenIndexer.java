package utils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.VersionType;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by aniruddha.g on 06/06/15.
 */
public class ZopNowKitchenIndexer {
    private static class Pojo {
        public String KitchenPage;
        public List<Category> Category;
    }
    
    private static class Category {
        @JsonProperty("Category-Title")
        public String CategoryTitle;
        public List<Product> Products;
    }
    
    private static class Product {
        public String ProductName;
        public String Brand;
        public String MRP;
        public String ActualPrice;
        public String Details;
        public List<Variant> variants;
    }
    
    private static class Variant {
        public String VolumeVariant;
        public String VariantPrice;
    }

    public static void main(String[] args) throws IOException {
        Client client;
        BulkRequestBuilder bulkRequest;

        ImmutableSettings.Builder builder = ImmutableSettings.settingsBuilder();
        Settings settings = builder
                .put("cluster.name", "bigfoot-preprod").build();

        client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress("bigfoot-preprod-0004.ch.flipkart.com", 9300));
        bulkRequest = client.prepareBulk();

        long timestamp = System.currentTimeMillis();
        String store1 = "zopnow";
        String store2 = "bigbasket";
        
        Pojo pojo = JsonUtils.EVENT.mapper.readValue(new File("/tmp/run_results_new.txt"), Pojo.class);
        String superCatName = pojo.KitchenPage;
        int i = 0;
        for (Category category : pojo.Category) {
            String catName = category.CategoryTitle;
            if (category.Products == null) {
                continue;
            }
            for (Product product : category.Products) {
                if (product.ProductName.equals("Forbidden")) {
                    continue;
                }
                String productName = product.ProductName;
                String brand = product.Brand;
                if (product.variants != null) {
                    if (product.variants.size() > 0) {
                        for (Variant variant : product.variants) {
                            Double price = Double.valueOf(variant.VariantPrice.split(" ")[1]);
                            String volume = variant.VolumeVariant;
                            Map<String,Object> document = Maps.newHashMap();
                            document.put("name", productName);
                            document.put("brand",brand);
                            document.put("volume",volume);
                            document.put("super_category", superCatName);
                            document.put("category", catName);
                            List<Store> stores = Lists.newArrayList();
                            stores.add(new Store(store1, price));
                            stores.add(new Store(store2, getAdjustedPrice(price, i)));
                            document.put("stores", stores);
                            System.out.println(JsonUtils.EVENT.toJson(document));
                            bulkRequest.add(client.prepareIndex("shop_eazy", "shop_eazy_data",productName)
                                    .setSource(JsonUtils.EVENT.toJson(document)).setTTL(604800000).setVersion(timestamp)
                                    .setVersionType(VersionType
                                            .EXTERNAL_GTE));
                            i++;
                        }
                    } else {
                        Double price = Double.valueOf(product.ActualPrice.split(" ")[1]);
                        Map<String,Object> document = Maps.newHashMap();
                        document.put("name", productName);
                        document.put("brand",brand);
                        document.put("volume",getVolumeFromProductName(productName));
                        document.put("super_category", superCatName);
                        document.put("category", catName);
                        List<Store> stores = Lists.newArrayList();
                        stores.add(new Store(store1, price));
                        stores.add(new Store(store2, getAdjustedPrice(price, i)));
                        document.put("stores", stores);
                        bulkRequest.add(client.prepareIndex("shop_eazy", "shop_eazy_data", productName)
                                .setSource(JsonUtils.EVENT.toJson(document)).setTTL(604800000).setVersion(timestamp)
                                .setVersionType(VersionType
                                        .EXTERNAL_GTE));
                        i++;
                    }
                } else {
                    Double price = Double.valueOf(product.ActualPrice != null ? product.ActualPrice.split(" ")[1] : product.MRP.split(" ")[1]);
                    Map<String,Object> document = Maps.newHashMap();
                    document.put("name", productName);
                    document.put("brand",brand);
                    document.put("volume",getVolumeFromProductName(productName));
                    document.put("super_category", superCatName);
                    document.put("category", catName);
                    List<Store> stores = Lists.newArrayList();
                    stores.add(new Store(store1, price));
                    stores.add(new Store(store2, getAdjustedPrice(price, i)));
                    document.put("stores", stores);
                    bulkRequest.add(client.prepareIndex("shop_eazy", "shop_eazy_data", productName)
                            .setSource(JsonUtils.EVENT.toJson(document)).setTTL(604800000).setVersion(timestamp)
                            .setVersionType(VersionType
                                    .EXTERNAL_GTE));
                    i++;
                }
                
            }
        }
        BulkResponse responses = null;
        try {
            if (bulkRequest.numberOfActions() > 0) {
                responses = bulkRequest.execute().actionGet();
            }
        } finally {
            if (responses != null) {
                if (responses.hasFailures()) {
                    System.out.println((responses.buildFailureMessage()));
                }
            }
            if (client != null) {
                client.close();
            }
        }
    }
    
    private static String getVolumeFromProductName(String productName) {
        String[] parts = productName.split(" ");
        int i = parts.length-1;
        for (; i >= 0; i--) {
//            if (parts[i].matches("-?\\d+(\\.\\d+)?")) break;
            if (Character.isDigit(parts[i].charAt(0))) break;
        }
        return Joiner.on(" ").join(Arrays.copyOfRange(parts, i, parts.length));
    }

    private static double getAdjustedPrice(Double price, int i) {
        if (i%2 == 0) {
            return 1.1*price;
        } else {
            return 0.9*price;
        }
    }

    public static class Store{
        public String store_name;
        public double price;

        public Store(String store_name, double price) {
            this.store_name = store_name;
            this.price = price;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Store)) return false;

            Store store = (Store) o;

            if (Double.compare(store.price, price) != 0) return false;
            if (!store_name.equals(store.store_name)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result;
            long temp;
            result = store_name.hashCode();
            temp = Double.doubleToLongBits(price);
            result = 31 * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "Store{" +
                    "store_name='" + store_name + '\'' +
                    ", price=" + price +
                    '}';
        }
    }
}
