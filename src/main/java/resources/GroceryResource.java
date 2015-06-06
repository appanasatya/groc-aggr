package resources;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import core.ElasticSearchDao;
import data.Product;
import data.StoreProductList;
import data.SurpriseListOfStores;
import data.UserProductList;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Produces(value = MediaType.APPLICATION_JSON)
@Path("/groceries")
public class GroceryResource {

    public static final String host = "bigfoot-preprod-0004.ch.flipkart.com";
    public static final int port = 9300;
    public static final String clusterName = "bigfoot-preprod";
    private static ElasticSearchDao elasticSearchDao = null;
    public static final String SINGLECOLUMN_QUERY_TEMPLATE =
            "{\"query\":{\"filtered\":{\"query\":{\"match\":{\"%s\":\"%s\"}}}}}";
    public static final String IN_QUERY_TEMPLATE = 
            "{\"query\":{\"bool\":{\"must\":[{\"ids\": {\"type\" : \"shop_eazy_data\", \"values\" : %s}}],\"must_not\":[],\"should\":[]}},\"from\":0,\"sort\":[],\"facets\":{}}";
    public static final String MATCH_ALL = "{\"query\":{\"match_all\":{}}}";

    @GET
    @Path("/superCategories")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Set<String>> getSuperCategories() {
        final String elasticSearchIndex = "shop_eazy";
        final String elasticSearchType = "shop_eazy_data";
        Map<String, Set<String>> superCategories = Maps.newHashMap();
        ElasticSearchDao elasticSearchDao = getElasticSearchDao();
        SearchResponse searchResponse = elasticSearchDao.execute(elasticSearchIndex, elasticSearchType, MATCH_ALL);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            String superCategory = searchHit.getSource().get("super_category").toString();
            superCategories.put(superCategory, getCategories(superCategory));
        }
        return superCategories;
    }

    public static ElasticSearchDao getElasticSearchDao() {
        if(elasticSearchDao == null) {
            elasticSearchDao = new ElasticSearchDao(host,port,Optional.fromNullable(clusterName));
        }
        return elasticSearchDao;
    }

    public String getJsonQuery(String key,String value) {
        return String.format(SINGLECOLUMN_QUERY_TEMPLATE,key,value);
    }

    @GET
    @Path("/categories/{superCategory}")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getCategories(@PathParam("superCategory") String superCategory) {
        final String elasticSearchIndex = "shop_eazy";
        final String elasticSearchType = "shop_eazy_data";
        Set<String> categories = new HashSet<String>();
        ElasticSearchDao elasticSearchDao = getElasticSearchDao();
        String jsonQuery = getJsonQuery("super_category", superCategory);
        SearchResponse searchResponse = elasticSearchDao.execute(elasticSearchIndex, elasticSearchType, jsonQuery);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            categories.add(searchHit.getSource().get("category").toString());
        }
        return categories;
    }

    @GET
    @Path("/products/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Product> getProducts(@PathParam("category") String category) {
        final String elasticSearchIndex = "shop_eazy";
        final String elasticSearchType = "shop_eazy_data";
        Set<Product> products = new HashSet<Product>();
        ElasticSearchDao elasticSearchDao = getElasticSearchDao();
        String jsonQuery = getJsonQuery("category",category);
        SearchResponse searchResponse = elasticSearchDao.execute(elasticSearchIndex, elasticSearchType,jsonQuery);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            products.add(getProduct(searchHit));
        }
        return products;
    }

    private Product getProduct(SearchHit searchHit) {
        double minStorePrice = Integer.MAX_VALUE;
        double maxStorePrice = 0;
//        ObjectMapper objectMapper = new ObjectMapper();

//            List<Store> stores = objectMapper.readValue(searchHit.getSource().get("stores").toString(),
//                    objectMapper.getTypeFactory().constructCollectionType(List.class, Store.class));
            List<Map<String,String>> stores = (List<Map<String,String>>) searchHit.getSource().get("stores");
            for (Map store : stores) {
                double storePrice = (Double) store.get("price");
                if (minStorePrice > storePrice  ) {
                    minStorePrice = storePrice;
                }
                if (maxStorePrice < storePrice) {
                    maxStorePrice = storePrice;
                }
            }
        return new Product(searchHit.getId().toString(),searchHit.getSource().get("super_category").toString(),
                searchHit.getSource().get("category").toString(),searchHit.getSource().get("name").toString(),
                searchHit.getSource().get("brand").toString(),searchHit.getSource().get("volume").toString(),
                minStorePrice,maxStorePrice);
    }

    @POST
    @Path("/allStores")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<StoreProductList> getAllStoreBaskets(UserProductList userProductList) {
        //Insert the request payload into elasticSearch
        //and compute allStoreBaskets and return the same
        final String elasticSearchIndex = "shop_eazy";
        final String elasticSearchType = "shop_eazy_data";
        Map<String, StoreProductList> storeProductListMap = Maps.newHashMap();
        ElasticSearchDao elasticSearchDao = getElasticSearchDao();
        Map<String, Integer> producQtyMap = Maps.newHashMap();
        for (String prodId : userProductList.productIds) {
            if(producQtyMap.containsKey(prodId)) {
                producQtyMap.put(prodId,producQtyMap.get(prodId) + 1);
            }
            else {
                producQtyMap.put(prodId,1);
            }
        }
        String jsonQuery = getInQuery(producQtyMap.keySet());
        SearchResponse searchResponse = elasticSearchDao.execute(elasticSearchIndex, elasticSearchType, jsonQuery);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            List<Map<String, Object>> stores = (List<Map<String,Object>>) searchHit.getSource().get("stores");
            for (Map<String,Object> store : stores) {
                if (!storeProductListMap.keySet().contains(store.get("store_name"))) {
                    storeProductListMap.put((String)store.get("store_name"), new StoreProductList((String)store.get("store_name")));
                }
                StoreProductList storeProductList = storeProductListMap.get((String)store.get("store_name"));
                storeProductList.addProduct(searchHit.getId());
                storeProductList.addToTotal(producQtyMap.get(searchHit.getId()) * (Double)store.get("price"));
            }
        }
        return Lists.newArrayList(storeProductListMap.values());
    }

    private String getInQuery(Set<String> productNames) {
        String value = "[\"" + Joiner.on("\",\"").join(productNames) + "\"]";
        return String.format(IN_QUERY_TEMPLATE,value);
    }

    @POST
    @Path("/smartBasket")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SurpriseListOfStores getSmartBaskets(UserProductList userProductList) {

        Double totalSavedAmount = 0.0;
        Double totalAmount = 0.0;
        final String elasticSearchIndex = "shop_eazy";
        final String elasticSearchType = "shop_eazy_data";
        Map<String, StoreProductList> storeProductListMap = Maps.newHashMap();
        ElasticSearchDao elasticSearchDao = getElasticSearchDao();
        Map<String, Integer> producQtyMap = Maps.newHashMap();
        for (String prodId : userProductList.productIds) {
            if(producQtyMap.containsKey(prodId)) {
                producQtyMap.put(prodId,producQtyMap.get(prodId) + 1);
            }
            else {
                producQtyMap.put(prodId,1);
            }
        }
        String jsonQuery = getInQuery(producQtyMap.keySet());
        SearchResponse searchResponse = elasticSearchDao.execute(elasticSearchIndex, elasticSearchType, jsonQuery);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            List<Map<String, Object>> stores = (List<Map<String,Object>>) searchHit.getSource().get("stores");
            double maxStorePrice = Integer.MIN_VALUE;
            double minStorePrice = Integer.MAX_VALUE;
            String minStoreName = "";
            for (Map<String,Object> store : stores) {
                if (!storeProductListMap.keySet().contains(store.get("store_name"))) {
                    storeProductListMap.put((String)store.get("store_name"), new StoreProductList((String)store.get("store_name")));
                }
                if (minStorePrice > (Double) store.get("price")) {
                    minStorePrice = (Double) store.get("price");
                    minStoreName = (String)store.get("store_name");
                }
                if (maxStorePrice < (Double) store.get("price")) {
                    maxStorePrice = (Double) store.get("price");
                }
            }
            StoreProductList storeProductList = storeProductListMap.get(minStoreName);
            storeProductList.addProduct(searchHit.getId());
            storeProductList.addToSaving(producQtyMap.get(searchHit.getId()) * (maxStorePrice - minStorePrice));
            totalSavedAmount = totalSavedAmount + producQtyMap.get(searchHit.getId()) * (maxStorePrice - minStorePrice);
            storeProductList.addToTotal(producQtyMap.get(searchHit.getId()) * minStorePrice);
            totalAmount = totalAmount +  producQtyMap.get(searchHit.getId()) * minStorePrice;
        }
        return new SurpriseListOfStores(Lists.newArrayList(storeProductListMap.values()),totalAmount,totalSavedAmount);
    }
}
