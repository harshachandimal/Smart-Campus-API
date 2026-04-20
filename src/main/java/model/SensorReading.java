package model;

public class SensorReading {

    private String id;
    private double value;
    private String timestamp;
    private String sensorId;

    public SensorReading() {}

    public SensorReading(String id, double value, String timestamp, String sensorId) {
        this.id = id;
        this.value = value;
        this.timestamp = timestamp;
        this.sensorId = sensorId;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public double getValue() { return value; }
    public void setValue(double value) { this.value = value; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getSensorId() { return sensorId; }
    public void setSensorId(String sensorId) { this.sensorId = sensorId; }
}
