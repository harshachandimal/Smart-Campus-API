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

---

### Part 1, Question 1 — JAX-RS Resource Lifecycle

**Q: Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? How does this impact the way you manage and synchronize your in-memory data structures?**

By default, JAX-RS uses a **per-request lifecycle**, meaning a brand-new instance of each resource class is created for every incoming HTTP request and destroyed once the response has been sent. This behaviour is governed by the `@RequestScoped` default in the JAX-RS specification. The key benefit is thread safety at the resource level — since no resource instance is shared between concurrent requests, any instance variables within a resource class are isolated and do not require synchronisation.

However, this per-request model does **not** eliminate all concurrency concerns. In this implementation, the shared static `DataStore` class holds the application-wide data store using plain `HashMap` instances (`rooms`, `sensors`, `sensorReadings`). Because `HashMap` is **not thread-safe**, concurrent write operations from two simultaneous requests (e.g., two POST requests creating different sensors at the same time) could cause data corruption, lost updates, or `ConcurrentModificationException` errors. In a production system, this would be resolved by replacing each `HashMap` with a `ConcurrentHashMap`, which provides atomic, thread-safe operations without requiring explicit `synchronized` blocks. Alternatively, the `@Singleton` annotation can be applied to a resource class to share one instance across all requests, but doing so requires all shared mutable state in that class to be fully synchronised manually.

---

### Part 1, Question 2 — HATEOAS and the Discovery Endpoint

**Q: Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**

HATEOAS (Hypermedia As The Engine Of Application State) is considered a hallmark of advanced REST design because it makes an API **self-describing at runtime**. Instead of requiring clients to have prior knowledge of all available URLs, each response includes hyperlinks to related actions and resources. Clients can dynamically navigate the entire API by following these embedded links, in the same way a user browses a website without memorising every URL.

Compared to static documentation, HATEOAS offers several key advantages to client developers. First, if the server changes a URL structure, clients that follow hypermedia links adapt automatically without any code changes — whereas clients relying on hardcoded URLs from static docs would immediately break. Second, static documentation can quickly become outdated or inconsistent with the actual running API; hypermedia links are always generated live from the server, so they are inherently accurate. Third, dynamic capabilities can be communicated contextually — for example, a `DELETE` link can be omitted from a response when deletion is not currently permitted, eliminating the need for clients to separately query permissions. In this API, the discovery endpoint (`GET /api/v1/`) implements this principle by providing `href` and supported `methods` for every resource, allowing a client to discover the full API surface from a single starting point without consulting any external document.

---

### Part 2, Question 1 — Returning IDs vs Full Room Objects

**Q: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.**

Returning only IDs (e.g., `["room1", "room2", "room3"]`) produces a very small HTTP response, minimising network bandwidth consumption. This is advantageous in high-latency or bandwidth-constrained environments. However, it places a significant burden on the **client side**: to display any useful information, the client must make one additional HTTP request per room ID to fetch its details. For a list of 100 rooms, this results in 101 total HTTP requests — the classic **N+1 problem** — which dramatically increases total latency and server load.

Returning full room objects (as implemented in this API via `GET /rooms`) sends all necessary data in a single response, eliminating the need for follow-up requests entirely. The client receives everything it needs to render the room list in one round trip. The trade-off is a larger individual response payload, but for most practical collection sizes this is the clearly superior approach. Where payload size does become a concern at scale, the standard solution is **pagination** (e.g., `?page=1&size=20`) or **sparse fieldsets** (e.g., `?fields=id,name`), which give clients control over what is returned without requiring the N+1 pattern.

---

### Part 2, Question 2 — Idempotency of DELETE

**Q: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**

Idempotency means that making the same request multiple times produces the **same resulting server state** as making it once. By this definition, the DELETE operation in this implementation **is idempotent**: after the first successful `DELETE /rooms/room1`, the room no longer exists in the data store. Any subsequent identical DELETE requests leave the server in exactly the same state — the room is still absent. No duplicate deletion, data corruption, or unintended side-effect occurs regardless of how many times the request is repeated.

It is important to clarify that **idempotency refers to server state, not response codes**. In this implementation, the first DELETE returns `200 OK`, but a second identical DELETE returns `404 Not Found` because the resource method checks `if (room == null)` before proceeding. The HTTP specification does not require the response status code to be identical across repeated calls — only the resulting state of the resource must be consistent. This behaviour is standard and correct. The `404` on repeat calls actually provides the client with useful feedback that the resource was already deleted, which is considered good REST practice. A non-idempotent operation (such as a poorly implemented DELETE that decrements a counter on each call) would cause different server state with each repetition, which is what idempotency is designed to prevent.

---

### Part 3, Question 1 — @Consumes Annotation and Content Negotiation

**Q: We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as `text/plain` or `application/xml`. How does JAX-RS handle this mismatch?**

The `@Consumes(MediaType.APPLICATION_JSON)` annotation instructs the JAX-RS runtime to **only accept requests whose `Content-Type` header is `application/json`**. When a client sends a request with `Content-Type: text/plain` or `Content-Type: application/xml`, JAX-RS performs content negotiation **before the resource method is ever invoked**. It inspects the incoming `Content-Type` header and checks whether any registered `MessageBodyReader` implementation is capable of deserialising that media type into the target Java object (e.g., `Sensor`). Since no `MessageBodyReader` is registered for `text/plain → Sensor` or `application/xml → Sensor`, the JAX-RS runtime immediately returns a **`415 Unsupported Media Type`** response without executing any business logic in the resource method.

This is significant for both data integrity and security. It ensures that malformed or incorrectly formatted payloads are rejected at the framework level rather than causing a `NullPointerException`, silent data corruption, or a misleading `500` error inside the resource method. By declaring `@Consumes` explicitly, the server communicates a clear contract to clients: only JSON will be accepted, and any violation will receive an immediately actionable, standardised error code.

---

### Part 3, Question 2 — @QueryParam vs Path Parameter for Filtering

**Q: You implemented filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?**

Using `@QueryParam` for filtering (e.g., `GET /sensors?type=temperature`) correctly treats `/sensors` as the **resource** and `type` as an optional modifier on how to retrieve members of that collection. This is semantically precise — all temperature sensors are still sensors; they are not a distinct resource with their own identity or URI path.

Embedding the filter value in the URL path (e.g., `GET /api/v1/sensors/type/CO2`) creates a **false resource hierarchy**, implying that `CO2` is a sub-resource of `type`, which is itself a sub-resource of `sensors`. This is semantically misleading and structurally problematic. Adding a second filter (e.g., status) would require a path like `/sensors/type/CO2/status/ACTIVE`, which is verbose, brittle, and non-standard.

Query parameters are superior for filtering for four key reasons:

1. **Optionality** — The base endpoint `GET /sensors` works without any query parameters; filters are additive and optional by design.
2. **Composability** — Multiple filters can be combined freely: `?type=temperature&status=ACTIVE`, without changing the resource path.
3. **Resource identity is preserved** — The URI `/sensors` always identifies the sensor collection. Query parameters modify the view of that collection without changing what resource is being addressed.
4. **Industry standard** — Query parameter filtering is the universally adopted convention in major REST APIs (GitHub, Stripe, Google Maps), making the API immediately familiar to developers.

---

### Part 4, Question 1 — Sub-Resource Locator Pattern

**Q: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?**

The Sub-Resource Locator pattern is a JAX-RS feature where a resource method does not handle the HTTP request itself but instead **returns a reference to another resource class** that will handle the remaining path segments. For example, `SensorResource` does not define `GET /sensors/{id}/readings` directly — it returns a `SensorReadingResource` instance after validating the sensor ID, and that class then handles all reading-related operations.

The primary architectural benefit is **separation of concerns** following the Single Responsibility Principle: each resource class has one focused job. `SensorResource` manages the lifecycle of sensors (creation, retrieval, filtering); `SensorReadingResource` manages historical reading data for a specific sensor. Neither class is aware of the other's internal logic.

In a large API with many nested resources, defining every route in a single controller would create a class with potentially hundreds of methods, becoming unmaintainable, unreadable, and a constant source of merge conflicts in team environments. The sub-resource locator pattern avoids this by:

- Allowing each resource class to be **independently developed, tested, and maintained**.
- Enabling the **parent to pass validated context** to the child — in this API, `SensorResource` confirms the sensor exists before instantiating `SensorReadingResource`, so the sub-resource never receives an invalid `sensorId`.
- Making the codebase **navigable** — a developer looking for reading-related logic immediately knows to look in `SensorReadingResource`, not buried in a 1000-line controller.
- Promoting **reusability** — the same sub-resource class can theoretically be instantiated from multiple parent paths without duplicating code.

---

### Part 5, Question 2 — 422 vs 404 for Missing Referenced Resource

**Q: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**

HTTP `404 Not Found` communicates that **the URI itself does not identify an existing resource** — the route the client is trying to access does not exist on the server. However, when a client sends `POST /sensors` with a valid JSON body containing an invalid `roomId`, the endpoint `/sensors` absolutely exists and is perfectly accessible. The problem is not with the URL but with the **semantic content of the request body**: the `roomId` field references a room that does not exist in the system.

Using `404` in this scenario would be misleading and confusing — it would imply to the client that the `/sensors` endpoint cannot be found, which is false. The client would likely assume it has constructed the wrong URL and waste time debugging its HTTP request rather than its payload content.

HTTP `422 Unprocessable Entity` is the correct and more semantically accurate status because it communicates: *"I received your request, I understood its structure, the JSON is syntactically valid, but I cannot complete the operation because the business logic failed — specifically, a referenced entity does not exist."* This precise distinction is critical for client developers who must write error-handling logic: a `422` tells them to inspect and correct the **contents of their payload**, while a `404` tells them to check their **URL**. These are two entirely different debugging paths, and using the wrong code forces unnecessary investigation.

---

### Part 5, Question 4 — Security Risks of Exposing Stack Traces

**Q: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**

Exposing raw Java stack traces in API error responses is a serious security vulnerability known as **information disclosure**. A stack trace reveals the internal structure of an application in ways that an attacker can directly exploit:

1. **Application architecture mapping** — Package and class names (e.g., `service.DataStore`, `resource.SensorResource`, `mapper.GenericExceptionMapper`) reveal how the codebase is organised, making it significantly easier for an attacker to understand the system's structure and identify high-value targets.

2. **Library and version fingerprinting** — Stack traces frequently include third-party framework class names such as `org.glassfish.jersey` or `org.glassfish.grizzly` along with version numbers. An attacker can look up these exact versions in public CVE (Common Vulnerabilities and Exposures) databases to find known, unpatched exploits that apply specifically to the deployed versions.

3. **Injection point discovery** — The exact class name and line number where an exception occurred can reveal how and where user-supplied data flows through the system, potentially identifying unsanitised input paths that are candidates for SQL injection, path traversal, or other injection attacks.

4. **Technology stack disclosure** — Knowing the server uses Grizzly HTTP server, Jersey JAX-RS, and Java allows an attacker to consult attack playbooks and tooling specific to those technologies.

The `GenericExceptionMapper` in this implementation mitigates all of these risks by catching every unhandled `Throwable` and returning a safe, generic `500 Internal Server Error` JSON response that contains no internal details whatsoever. The actual exception and stack trace are only written to the **server-side log**, accessible only to authorised developers — never to the external caller.

---

### Part 5, Question 5 — JAX-RS Filters vs Manual Logging

**Q: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?**

Manually inserting `Logger.info()` calls inside every resource method is an anti-pattern that violates the **DRY (Don't Repeat Yourself)** principle and the **Separation of Concerns** design principle. The problems this approach creates are significant:

- **Code duplication** — Logging code must be copied into every single resource method. With five resource classes and multiple methods each, this results in dozens of identical or near-identical log statements scattered throughout the codebase.
- **Inconsistency risk** — If a developer forgets to add logging to a new endpoint, that endpoint becomes a blind spot in the logs. There is no mechanism to enforce that every request is logged.
- **Maintenance burden** — If the log format needs to change (e.g., adding a request ID or timestamp format), every single method across every resource class must be edited individually.
- **Business logic pollution** — Resource methods become cluttered with infrastructure concerns (logging), making them harder to read, understand, and unit test.

JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter` solve all of these problems by implementing logging exactly **once**, in a single dedicated class (`LoggingFilter`), that automatically intercepts **every** HTTP request and response across the entire API. The `@Provider` annotation registers it globally with zero modifications required in any resource class. This approach guarantees consistent, complete logging coverage. When a new endpoint is added in the future, it is automatically covered by the filter with no additional effort. This pattern exemplifies the **cross-cutting concerns** approach, where infrastructure behaviour (logging, authentication, CORS) is handled at a layer that surrounds all business logic rather than being embedded within it.

