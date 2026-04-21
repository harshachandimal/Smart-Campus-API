package mapper;

import exception.SensorUnavailableException;
import model.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableMapper implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException ex) {

        ErrorResponse error = new ErrorResponse(
                403,
                ex.getMessage()
        );

        return Response.status(Response.Status.FORBIDDEN)
                .entity(error)
                .build();
    }
}
