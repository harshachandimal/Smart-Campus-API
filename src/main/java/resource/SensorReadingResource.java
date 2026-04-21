package resource;

import exception.SensorUnavailableException;
import model.Sensor;
import model.SensorReading;
import service.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private String sensorId;

    // Constructor receives sensorId from parent SensorResource
    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // GET all readings for this sensor (historical data)
    @GET
    public List<SensorReading> getReadings() {

        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            throw new NotFoundException("Sensor not found");
        }

        List<SensorReading> readings = DataStore.sensorReadings.get(sensorId);

        if (readings == null) {
            return new ArrayList<>();
        }

        return readings;
    }

    // POST a new reading for this sensor
    @POST
    public Response addReading(SensorReading reading) {

        Sensor sensor = DataStore.sensors.get(sensorId);

        if (sensor == null) {
            throw new NotFoundException("Sensor not found");
        }

        // State Constraint: Sensors in MAINTENANCE cannot accept new readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor " + sensorId + " is currently under MAINTENANCE and cannot accept new readings."
            );
        }

        // Set the sensorId on the reading
        reading.setSensorId(sensorId);

        // Add reading to the history list
        DataStore.sensorReadings
                .computeIfAbsent(sensorId, k -> new ArrayList<>())
                .add(reading);

        // Side Effect: Update the currentValue on the parent Sensor
        sensor.setCurrentValue(reading.getValue());

        return Response.status(Response.Status.CREATED).entity(reading).build();
    }
}
