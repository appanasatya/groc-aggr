package core;

import io.dropwizard.Application;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import resources.GroceryResource;

public class GrocService extends Application<ServiceConfiguration>{

    /**
     * When the application runs, this is called after the {@link io.dropwizard.Bundle}s are run. Override it to add
     * providers, resources, etc. for your application.
     *
     * @param configuration the parsed {@link io.dropwizard.Configuration} object
     * @param environment   the application's {@link io.dropwizard.setup.Environment}
     * @throws Exception if something goes wrong
     */
    @Override
    public void run(ServiceConfiguration configuration, Environment environment) throws Exception {
        environment.jersey().register(new GroceryResource());
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        bootstrap.addBundle(new MultiPartBundle());
    }

    public static void main(String[] args) throws Exception {
        new GrocService().run(args);
    }
}
