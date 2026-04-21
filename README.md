# Smart Campus Management API

A RESTful API for managing campus rooms, IoT sensors, and sensor readings — built with **Java**, **JAX-RS (Jersey 2.41)**, and the **Grizzly HTTP Server**.

---

## Table of Contents

1. [API Design Overview](#api-design-overview)
2. [Build & Launch Instructions](#build--launch-instructions)
3. [Sample curl Commands](#sample-curl-commands)
4. [Conceptual Report](#conceptual-report)

---

## API Design Overview

### Architecture

This API is built using the following technology stack:

| Component | Technology |
|-----------|-----------|
| Language | Java 24 |
| REST Framework | JAX-RS via Jersey 2.41 |
| HTTP Server | Eclipse Grizzly (embedded) |
| Data Format | JSON (via `jersey-media-json-binding`) |
| Build Tool | Apache Maven |
| Data Storage | In-memory (`HashMap` — no database) |

The application starts a **standalone Grizzly HTTP server** on port `8080`. There is no external web server (e.g., Tomcat) required — the JAX-RS application is bootstrapped entirely via `Main.java`.

---

### Resource Hierarchy

The API follows a clear REST resource hierarchy:

```
/api/v1/
├── /                          → Discovery (Part 1)
├── /rooms                     → Room collection (Part 2)
│   ├── /{id}                  → Single room (Part 2)
│   └── /{roomId}/sensors      → Sub-resource: sensors in a room (Part 4)
│       └── /{sensorId}        → Update a specific sensor in a room (Part 4)
└── /sensors                   → Sensor collection (Part 3)
    └── /{sensorId}/readings   → Sub-resource: sensor reading history (Part 4)
```

All endpoints consume and produce `application/json`.

---

### Data Models

**Room**
```json
{
  "id": "room1",
  "name": "Lab A",
  "capacity": 30,
  "sensorIds": ["s1", "s2"]
}
```

**Sensor**
```json
{
  "id": "s1",
  "type": "temperature",
  "status": "ACTIVE",
  "currentValue": 22.5,
  "roomId": "room1"
}
```
> Valid status values: `ACTIVE`, `MAINTENANCE`, `OFFLINE`

**SensorReading**
```json
{
  "id": "r1",
  "value": 24.1,
  "timestamp": "2025-04-21T10:00:00",
  "sensorId": "s1"
}
```

---

### Error Handling Strategy

All errors are returned as structured JSON using custom **JAX-RS Exception Mappers**:

```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Room not found"
}
```

| Exception | HTTP Status | Scenario |
|-----------|-------------|----------|
| `NotFoundException` | `404 Not Found` | Resource does not exist |
| `LinkedResourceNotFoundException` | `422 Unprocessable Entity` | Referenced resource (e.g. roomId) does not exist |
| `RoomNotEmptyException` | `409 Conflict` | Deleting a room that still has sensors assigned |
| `SensorUnavailableException` | `503 Service Unavailable` | Posting a reading to a sensor in `MAINTENANCE` status |
| Any uncaught `Exception` | `500 Internal Server Error` | Global fallback handler |

A `LoggingFilter` automatically logs every incoming HTTP request (method + URI) and every outgoing response (status code) to the server console.

---

## Build & Launch Instructions

### Prerequisites

Ensure the following are installed before building:

- **Java JDK 24** (or compatible version) — verify with `java -version`
- **Apache Maven 3.x** — verify with `mvn -version`
- **Git** — to clone the repository

---

### Step-by-Step Instructions

**Step 1 — Clone the repository**

```bash
git clone https://github.com/harshachandimal/Smart-Campus-API.git
cd Smart-Campus-API
```

**Step 2 — Build the project**

Run the following Maven command to compile the project and download all dependencies:

```bash
mvn clean compile
```

You should see `BUILD SUCCESS` at the end of the output.

**Step 3 — Launch the server**

Start the embedded Grizzly HTTP server by running the `Main` class:

```bash
mvn exec:java -Dexec.mainClass="Main"
```

You should see the following output in your terminal:

```
Server started at http://localhost:8080/api/v1/
```

**Step 4 — Verify the server is running**

Open your browser or run this curl command to confirm the API is live:

```bash
curl -X GET http://localhost:8080/api/v1/
```

You should receive a JSON response describing the API and its available resources.

**Step 5 — To stop the server**

Press `Ctrl + C` in the terminal to shut down the Grizzly server.

---

## Sample curl Commands

The following curl commands demonstrate successful interactions across all parts of the API. Run them **in order** so that the required data (rooms, sensors) exists before dependent requests are made.

---

### 1. Discover the API (Part 1)

```bash
curl -X GET http://localhost:8080/api/v1/
```

**Expected Response:** `200 OK` with API metadata and HATEOAS-style resource links.

---

### 2. Create a Room (Part 2)

```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"room1\",\"name\":\"Lab A\",\"capacity\":30}"
```

**Expected Response:** `201 Created` with the created room object.

---

### 3. Get a Room by ID (Part 2)

```bash
curl -X GET http://localhost:8080/api/v1/rooms/room1
```

**Expected Response:** `200 OK` with the room details.

---

### 4. Create a Sensor (Part 3)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"s1\",\"type\":\"temperature\",\"status\":\"ACTIVE\",\"currentValue\":22.5,\"roomId\":\"room1\"}"
```

**Expected Response:** `201 Created` with the sensor object. The sensor is automatically linked to `room1`.

---

### 5. Filter Sensors by Type (Part 3)

```bash
curl -X GET "http://localhost:8080/api/v1/sensors?type=temperature"
```

**Expected Response:** `200 OK` with a list of all sensors of type `temperature`.

---

### 6. Get All Sensors in a Room — Sub-Resource (Part 4)

```bash
curl -X GET http://localhost:8080/api/v1/rooms/room1/sensors
```

**Expected Response:** `200 OK` with a list of sensor objects assigned to `room1`.

---

### 7. Post a Sensor Reading — Sub-Resource (Part 4)

```bash
curl -X POST http://localhost:8080/api/v1/sensors/s1/readings \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"r1\",\"value\":24.1,\"timestamp\":\"2025-04-21T10:00:00\"}"
```

**Expected Response:** `201 Created`. As a side-effect, the parent sensor's `currentValue` is automatically updated to `24.1`.

---

### 8. Get All Readings for a Sensor (Part 4)

```bash
curl -X GET http://localhost:8080/api/v1/sensors/s1/readings
```

**Expected Response:** `200 OK` with the historical readings list for sensor `s1`.

---

### 9. Error — Delete a Room That Has Sensors (Part 5)

```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/room1
```

**Expected Response:** `409 Conflict` — because sensor `s1` is still assigned to `room1`.

---

### 10. Error — Post a Reading to a Sensor in MAINTENANCE (Part 5)

First, update the sensor status to MAINTENANCE:

```bash
curl -X PUT http://localhost:8080/api/v1/rooms/room1/sensors/s1 \
  -H "Content-Type: application/json" \
  -d "{\"status\":\"MAINTENANCE\",\"currentValue\":22.5}"
```

Then try to post a new reading:

```bash
curl -X POST http://localhost:8080/api/v1/sensors/s1/readings \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"r2\",\"value\":30.0,\"timestamp\":\"2025-04-21T11:00:00\"}"
```

**Expected Response:** `503 Service Unavailable` — sensor is under MAINTENANCE and cannot accept readings.

---

### 11. Error — Create a Sensor with a Non-Existent Room (Part 5)

```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\":\"s99\",\"type\":\"humidity\",\"status\":\"ACTIVE\",\"currentValue\":55.0,\"roomId\":\"nonExistentRoom\"}"
```

**Expected Response:** `422 Unprocessable Entity` — the referenced room does not exist.

---

## Conceptual Report

### Part 1 — Setup & Discovery

**Q: What is the role of the discovery endpoint in a RESTful API, and how does it support the principles of HATEOAS?**

The discovery endpoint (`GET /api/v1/`) acts as the entry point of the API, providing clients with a structured map of all available resources and their supported HTTP methods. This implements the HATEOAS (Hypermedia As The Engine Of Application State) architectural constraint — a key REST principle — by embedding navigation links (`href`) directly in the response. Rather than requiring clients to hard-code URLs, they can dynamically discover and navigate the API from this single root endpoint. This makes the API self-documenting, loosely coupled, and more resilient to future URL changes.

---

### Part 2 — Room Management

**Q: Why is it important to validate whether a room has sensors before allowing deletion, and how does this relate to referential integrity in REST APIs?**

In a campus management system, sensors are linked to rooms via the `roomId` field. If a room is deleted while sensors still reference it, those sensors would become orphaned — pointing to a resource that no longer exists. This violates referential integrity, which is the guarantee that all references between resources are valid and consistent. By throwing a `RoomNotEmptyException` (mapped to `409 Conflict`) when a room still has active sensors, the API enforces this constraint at the application level, preventing data inconsistency. This mirrors how relational databases use foreign key constraints to prevent cascading data corruption.

---

### Part 3 — Sensors & Filtering

**Q: How does query parameter-based filtering improve the usability of a collection endpoint, and what are its advantages over returning all records?**

Query parameter filtering (e.g., `GET /sensors?type=temperature`) allows clients to retrieve only the subset of data they need without requiring a separate endpoint for each filter combination. This reduces network payload size, improves response times, and lowers processing overhead on the client side. Returning all records unconditionally is impractical as data grows — a campus with hundreds of sensors would produce an unmanageable response. Filtering follows the REST principle of designing collection endpoints to be flexible and composable, allowing future filters (e.g., `?status=ACTIVE`) to be added without breaking existing clients.

---

### Part 4 — Sub-Resources

**Q: What are the benefits of modelling sensor readings as a sub-resource of sensors, and how does the side-effect update of `currentValue` reflect real-world system design?**

Modelling readings as a sub-resource (`/sensors/{sensorId}/readings`) clearly expresses the ownership relationship — readings belong to a specific sensor and cannot exist independently. This design is semantically correct and aligns with REST best practices for hierarchical data. The sub-resource locator pattern in JAX-RS (`@Path` on a method returning a resource class) enables this nesting cleanly without duplicating route logic.

The side-effect of updating the parent sensor's `currentValue` whenever a new reading is posted reflects real-world IoT system design, where a sensor object always exposes its latest measurement. Rather than requiring clients to fetch the entire reading history to determine the current state, they can query `GET /sensors/{id}` directly. This is a common pattern in event-driven systems where writes to sub-resources trigger state transitions in parent resources.

---

### Part 5 — Error Handling & Logging

**Q: Why is centralised exception handling through `ExceptionMapper` preferable to handling errors inside each individual resource method, and what is the significance of request/response logging in a production REST API?**

Centralised exception handling via JAX-RS `ExceptionMapper` provides a single, consistent location to define how each exception type is translated into an HTTP response. Without it, error handling logic would be duplicated across every resource method, increasing the risk of inconsistent error formats and missed cases. For example, if a `NotFoundException` can be thrown from five different endpoints, a single `NotFoundMapper` ensures all five return the exact same JSON response structure and HTTP status code. This also means the resource code stays clean and focused on business logic, rather than being cluttered with error-formatting boilerplate.

Request and response logging via `ContainerRequestFilter` and `ContainerResponseFilter` is essential in a production REST API for observability and debugging. Logging every incoming request (method, URI) and every outgoing response (status code) creates an audit trail that helps developers diagnose issues such as unexpected 4xx/5xx errors, abnormal traffic patterns, and latency problems — without needing to reproduce the issue locally. This pattern is the foundation for more advanced API gateway logging, monitoring dashboards, and alerting systems.
