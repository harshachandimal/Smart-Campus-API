package resource;

import model.Sensor;
import model.Room;
import service.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomSensorResource {

    private String roomId;

    // Constructor receives roomId from parent
    public RoomSensorResource(String roomId) {
        this.roomId = roomId;
    }

    // ✅ GET sensors in a specific room
    @GET
    public List<Sensor> getRoomSensors() {

        Room room = DataStore.rooms.get(roomId);

        if (room == null) {
            throw new NotFoundException("Room not found");
        }

        return room.getSensorIds().stream()
                .map(id -> DataStore.sensors.get(id))
                .collect(Collectors.toList());
    }

    // ✅ UPDATE sensor inside room
    @PUT
    @Path("/{sensorId}")
    public Response updateSensor(@PathParam("sensorId") String sensorId, Sensor updatedSensor) {

        Room room = DataStore.rooms.get(roomId);

        if (room == null) {
            throw new NotFoundException("Room not found");
        }

        if (!room.getSensorIds().contains(sensorId)) {
            throw new NotFoundException("Sensor not in this room");
        }

        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            throw new NotFoundException("Sensor not found");
        }

        // 🔥 Update only relevant fields
        sensor.setStatus(updatedSensor.getStatus());
        sensor.setCurrentValue(updatedSensor.getCurrentValue());

        return Response.ok(sensor).build();
    }
}