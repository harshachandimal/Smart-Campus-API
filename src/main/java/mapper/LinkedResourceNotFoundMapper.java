package mapper;

import exception.LinkedResourceNotFoundException;
import model.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class LinkedResourceNotFoundMapper implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException ex) {

        ErrorResponse error = new ErrorResponse(
                422,
                ex.getMessage()
        );

        return Response.status(422)
                .entity(error)
                .build();
    }
}