package resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

@Path("/")
public class DiscoveryResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getApiInfo() {

        Map<String, Object> response = new LinkedHashMap<>();

        // API-level metadata
        response.put("name", "Smart Campus Management API");
        response.put("version", "v1");
        response.put("description", "A RESTful API for managing campus rooms, sensors, and sensor readings.");
        response.put("status", "operational");
        response.put("contact", "admin@smartcampus.com");

        // HATEOAS-style resource map
        Map<String, Object> resources = new LinkedHashMap<>();

        // Rooms resource
        Map<String, Object> rooms = new LinkedHashMap<>();
        rooms.put("href", "/api/v1/rooms");
        rooms.put("methods", Arrays.asList("GET", "POST"));
        rooms.put("description", "List all rooms or create a new room.");

        Map<String, Object> roomById = new LinkedHashMap<>();
        roomById.put("href", "/api/v1/rooms/{id}");
        roomById.put("methods", Arrays.asList("GET", "DELETE"));
        roomById.put("description", "Retrieve or delete a specific room by ID. DELETE is blocked if active sensors are assigned.");
        rooms.put("byId", roomById);

        Map<String, Object> roomSensors = new LinkedHashMap<>();
        roomSensors.put("href", "/api/v1/rooms/{roomId}/sensors");
        roomSensors.put("methods", Arrays.asList("GET", "PUT"));
        roomSensors.put("description", "List sensors assigned to a room, or update a specific sensor within a room.");
        rooms.put("sensors", roomSensors);

        resources.put("rooms", rooms);

        // Sensors resource
        Map<String, Object> sensors = new LinkedHashMap<>();
        sensors.put("href", "/api/v1/sensors");
        sensors.put("methods", Arrays.asList("GET", "POST"));
        sensors.put("description", "List all sensors (supports ?type= filter) or register a new sensor. roomId must reference an existing room.");

        Map<String, Object> sensorReadings = new LinkedHashMap<>();
        sensorReadings.put("href", "/api/v1/sensors/{sensorId}/readings");
        sensorReadings.put("methods", Arrays.asList("GET", "POST"));
        sensorReadings.put("description", "Retrieve reading history or post a new reading. POST is blocked if sensor status is MAINTENANCE.");
        sensors.put("readings", sensorReadings);

        resources.put("sensors", sensors);

        response.put("resources", resources);

        return response;
    }
}