package resource;

import model.Sensor;
import model.Room;
import service.DataStore;
import exception.LinkedResourceNotFoundException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    //  GET all sensors (with filtering)
    @GET
    public List<Sensor> getSensors(@QueryParam("type") String type) {

        if (type == null) {
            return DataStore.sensors.values().stream().collect(Collectors.toList());
        }

        return DataStore.sensors.values().stream()
                .filter(sensor -> sensor.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    //  POST create sensor
    @POST
    public Response createSensor(Sensor sensor) {

        //  VALIDATION: Room must exist
        Room room = DataStore.rooms.get(sensor.getRoomId());

        if (room == null) {
            throw new LinkedResourceNotFoundException("Room does not exist");
        }

        //  Prevent duplicate sensor
        if (DataStore.sensors.containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("Sensor already exists")
                    .build();
        }

        // Save sensor
        DataStore.sensors.put(sensor.getId(), sensor);

        //  IMPORTANT: Link sensor to room
        room.getSensorIds().add(sensor.getId());

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }
}