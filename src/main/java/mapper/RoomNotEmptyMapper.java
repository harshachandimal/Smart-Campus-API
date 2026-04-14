package mapper;

import exception.RoomNotEmptyException;
import model.ErrorResponse;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException ex) {

        ErrorResponse error = new ErrorResponse(
                409,
                ex.getMessage()
        );

        return Response.status(409)
                .entity(error)
                .build();
    }
}
