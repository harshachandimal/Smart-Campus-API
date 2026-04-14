package config;

import mapper.GenericExceptionMapper;
import mapper.LinkedResourceNotFoundMapper;
import mapper.NotFoundMapper;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        // 🔥 Register ALL packages from root
        packages("resource", "exception", "mapper", "model", "service");

        // 🔥 MANUAL REGISTRATION (guaranteed to work)
        register(LinkedResourceNotFoundMapper.class);
        register(NotFoundMapper.class);
        register(GenericExceptionMapper.class);
    }
}