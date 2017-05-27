package com.pst;

import com.pst.resources.RootResource;
import com.pst.resources.FileUploadResource;
import com.pst.health.RootHealthCheck;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.glassfish.jersey.media.multipart.MultiPartFeature;

public class PstServiceApplication extends Application<PstServiceConfiguration> {

    public static void main(final String[] args) throws Exception {
        new PstServiceApplication().run(args);
    }

    @Override
    public String getName() {
        return "PstService";
    }

    @Override
    public void initialize(final Bootstrap<PstServiceConfiguration> bootstrap) {
        // TODO: application initialization
    }

    @Override
    public void run(final PstServiceConfiguration configuration, final Environment environment) {
      final RootHealthCheck healthCheck = new RootHealthCheck(configuration.getWelcome());
      final RootResource root = new RootResource(configuration.getWelcome(), configuration.getDefaultName());
      final FileUploadResource uploader = new FileUploadResource(configuration.getUploadMessage());
      
      environment.healthChecks().register("root", healthCheck);
      environment.jersey().register(MultiPartFeature.class);
      environment.jersey().register(root);
      environment.jersey().register(uploader);
    }

}
