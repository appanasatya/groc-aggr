package resources;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
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

    @GET
    @Path("/superCategories")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<String> getSuperCategories() {
        final String elasticSearchIndex = "shop_eazy";
        final String elasticSearchType = "shop_eazy_data";
        Set<String> superCategories = new HashSet<String>();
        ElasticSearchDao elasticSearchDao = getElasticSearchDao();
        String jsonQuery = getJsonQuery("unit", "g");
        SearchResponse searchResponse = elasticSearchDao.execute(elasticSearchIndex, elasticSearchType, jsonQuery);
        SearchHit[] searchHits = searchResponse.getHits().getHits();
        for (SearchHit searchHit : searchHits) {
            superCategories.add(searchHit.getSource().get("brand").toString());
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
        return ImmutableSet.of();
    }

    @GET
    @Path("/products/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Product> getProducts(@PathParam("category") String category) {
        return ImmutableList.of();
    }

    @POST
    @Path("/allStores")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<StoreProductList> getAllStoreBaskets(UserProductList userProductList) {
        //Insert the request payload into elasticSearch
        //and compute allStoreBaskets and return the same
        return null;
    }

    @POST
    @Path("/smartBasket")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public SurpriseListOfStores getSmartBaskets(UserProductList userProductList) {
        //No need to Insert requestPayload into Es
        //Compute Surprise List and pass on to UI
        return null;
    }
}
