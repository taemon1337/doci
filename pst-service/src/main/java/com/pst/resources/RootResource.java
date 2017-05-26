package com.pst.resources;

import com.pst.api.Welcome;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Optional;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class RootResource {
    private final String message;
    private final String defaultName;
    private final AtomicLong counter;

    public RootResource(String message, String defaultName) {
        this.message = message;
        this.defaultName = defaultName;
        this.counter = new AtomicLong();
    }

    @GET
    @Timed
    public Welcome sayHello(@QueryParam("name") Optional<String> name) {
        final String value = String.format(message, name.orElse(defaultName));
        return new Welcome(counter.incrementAndGet(), value);
    }
}
