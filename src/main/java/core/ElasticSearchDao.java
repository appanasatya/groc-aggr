package core;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.ClearScrollRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.elasticsearch.common.settings.ImmutableSettings.settingsBuilder;

public class ElasticSearchDao {

    public static final String CLUSTER_NAME_PTY = "cluster.name";
    private final Client client;

    public ElasticSearchDao (final String host,
                             final int port,
                             com.google.common.base.Optional<String> clusterNameOptional) {
        final ImmutableSettings.Builder builder = settingsBuilder();
        Settings settings = builder.build();
        if (clusterNameOptional.isPresent()) {
            settings = builder
                    .put(CLUSTER_NAME_PTY, clusterNameOptional.get()).build();
        }
        client = new TransportClient(settings)
                .addTransportAddress(new InetSocketTransportAddress(host, port));
    }

    public SearchResponse execute(String index, String type, String jsonQuery) {
        try {
            System.out.println(index + ":" + type + ":" + jsonQuery);
            final SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index)
                    .setTypes(type)
                    .setSource(jsonQuery);
            final SearchResponse searchResponse = searchRequestBuilder.execute().get();
            if(searchResponse.isTimedOut()) {
                throw new RuntimeException("Query timed out");
            }
            return searchResponse;
        } catch (Exception e) {
            throw new RuntimeException("Query execution failed", e);
        }
    }

    public IndexResponse loadDocument(String index, String type, Map<String,Object> jsonMap) {
        try {
            final IndexRequestBuilder indexRequestBuilder = client.prepareIndex(index, type).setSource(jsonMap);
            IndexResponse indexResponse = indexRequestBuilder.execute().actionGet();
            if (!indexResponse.isCreated()) {
                throw new RuntimeException("index was not created ");
            }
            return indexResponse;
        }
        catch (Exception e) {
            throw new RuntimeException("document Load failed ");
        }

    }

    public BulkResponse loadDocuments(String index, String type, List<Map<String,Object>> listOfJsons) {
        try {
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            for (Map<String, Object> jsonMap : listOfJsons) {
                bulkRequest.add(client.prepareIndex(index, type).setSource(jsonMap));
            }
            BulkResponse bulkResponse = bulkRequest.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                throw new RuntimeException("Bulk Load of Jsons failed ");
            }
            return bulkResponse;
        }
        catch (Exception e) {
            throw new RuntimeException("Bulk Load of Jsons failed ");
        }

    }

    public SearchResponse scroll(String scrollId) {
        try {
            return client
                    .prepareSearchScroll(scrollId)
                    .setScroll("1m")
                    .execute().get();
        } catch (Exception e) {
            throw new RuntimeException("Cannot scroll, " + e);
        }
    }

    public void deleteScrolls(List<String> scrollIds) {
        final ClearScrollRequestBuilder clearScrollRequestBuilder = client.prepareClearScroll();
        for (String scrollId : scrollIds) {
            clearScrollRequestBuilder.addScrollId(scrollId);
        }
        try {
            clearScrollRequestBuilder.execute();
        } catch (Exception e) {
            Logger.getLogger("Elastic").log(Level.WARNING, "Cannot delete scroll..");
        }
    }

    public SearchResponse executeAndScroll(String index, String type, String jsonQuery) {
        try {
            System.out.println(index + ":" + type + ":" + jsonQuery);
            final SearchRequestBuilder searchRequestBuilder = client.prepareSearch(index)
                    .setTypes(type)
                    .setScroll("1m")
                    .setSource(jsonQuery);
            return searchRequestBuilder.execute().get();
        } catch (Exception e) {
            throw new RuntimeException("Query execution failed, " + e);
        }
    }

    public Object explain(Object nativeQuery) {
        return "TBD";
    }
}