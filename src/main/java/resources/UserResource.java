package resources;

import com.google.common.collect.ImmutableList;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Produces(value = MediaType.APPLICATION_JSON)
@Path("/users")
public class UserResource {

    @GET
    public List<String> getAvailableUsers() {
        List<String> list = ImmutableList.of("Harish", "Satya");
        return list;
    }
}
