package config;

import filter.LoggingFilter;
import mapper.GenericExceptionMapper;
import mapper.LinkedResourceNotFoundMapper;
import mapper.NotFoundMapper;
import mapper.RoomNotEmptyMapper;
import mapper.SensorUnavailableMapper;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class ApplicationConfig extends ResourceConfig {
    public ApplicationConfig() {
        // Register ALL packages from root
        packages("resource", "exception", "mapper", "model", "service", "filter");

        // MANUAL REGISTRATION (guaranteed to work)
        register(LinkedResourceNotFoundMapper.class);
        register(NotFoundMapper.class);
        register(GenericExceptionMapper.class);
        register(RoomNotEmptyMapper.class);
        register(SensorUnavailableMapper.class);
        register(LoggingFilter.class);
    }
}