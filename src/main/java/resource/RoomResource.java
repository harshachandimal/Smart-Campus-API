package resource;

import model.Room;
import service.DataStore;
import exception.RoomNotEmptyException;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;


@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class RoomResource {

    // GET all rooms
    @GET
    public Collection<Room> getAllRooms() {
        return DataStore.rooms.values();
    }

    //  POST create room — returns 201 Created with Location header
    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {

        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Room ID is required")
                    .build();
        }

        if (DataStore.rooms.containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Room already exists")
                    .build();
        }

        DataStore.rooms.put(room.getId(), room);

        // Build the Location URI pointing to the newly created room
        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();

        return Response.status(Response.Status.CREATED)
                .entity(room)
                .location(location)
                .build();
    }

    //  GET room by ID
    @GET
    @Path("/{id}")
    public Response getRoom(@PathParam("id") String id) {
        Room room = DataStore.rooms.get(id);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Room not found")
                    .build();
        }

        return Response.ok(room).build();
    }

    //  DELETE room
    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {

        Room room = DataStore.rooms.get(id);

        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Room not found")
                    .build();
        }

        // ! IMPORTANT LOGIC
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException("Room has sensors");
        }

        DataStore.rooms.remove(id);

        return Response.ok("Room deleted successfully").build();
    }

    @Path("/{roomId}/sensors")
    public RoomSensorResource getRoomSensorResource(@PathParam("roomId") String roomId) {
        return new RoomSensorResource(roomId);
    }
}