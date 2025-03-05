# Finagle Assignment

This project is a solution to the [Marvel Characters Aggregation Service](Assignment.pdf) assignment. It implements a
Finagle-based (Finch) REST API that integrates with the Marvel API handling paginated results and incorporates in-memory
caching.

## Tech Stack & Design Choices

### **Frameworks & Libraries**

- **Finch** for the REST API
    - Provides an opinionated and streamlined use of Finagle, making it easier to get started.
    - Natively integrates with Cats Effect.
    - Exposes Finagle client features, enabling easy integration with the Marvel API.
- **Circe** for JSON encoding/decoding
    - Its automatic derivation capabilities are sufficient for this project's scope and complexity.

### **Caching Mechanism**

- Utilizes `scala.collection.concurrent.TrieMap` as an in-memory cache.
    - Simple yet thread-safe, making it a suitable choice for a low-load project.
    - Provides an easy-to-use API for caching operations (`.get` / `.set`).

### **Implemented Features**

- **Support for Paginated Results**
- **Error Handling**
    - Guarantees that all [MarvelCharactersEndpoint](src/main/scala/com/lucas/marvel/api/MarvelCharactersEndpoint.scala)
      responses are valid JSON, even in failure scenarios.
    - Propagates Marvel API errors (e.g., invalid API key, quota exceeded) to the client.
    - Masks internal errors with a generic `500 Internal Server Error` to avoid exposing system details.
    - Some non-primary endpoints (e.g., Health Status, Swagger Docs) may return plain strings in the repone.
- **OpenAPI Specification**
    - Swagger UI is implemented to serve API documentation via a static JSON spec.
    - See [swagger.json](src/main/resources/swagger.json) and [swagger-ui](src/main/resources/swagger-ui).
- **Logging**
    - Logs have been integrated mainly to provide better insights into cache operations and pagination logic.
    - Debug logs are available in:
        - [CacheServiceImpl.scala](src/main/scala/com/lucas/marvel/services/impl/CacheServiceImpl.scala)
        - [PaginationHelpers.scala](src/main/scala/com/lucas/marvel/client/helpers/PaginationHelpers.scala)

## Features I wish I had the time to implement

- [ ] Better and more comprehensive unit and integration tests
- [ ] Externalizing more configuration parameters into [application.conf](src/main/resources/application.conf).
- [ ] Implementing a Retry Policy for Marvel API requests.
- [ ] Load testing using Gatling.
- [ ] Metrics collection using Finagle's built-in API.
- [ ] Basic HTTP authentication ([finagle-http-auth](https://github.com/finagle/finagle-http-auth)).

---

## How to Run the Project

### **Prerequisites**

- A valid **private key** and **public key** for the Marvel API.
- These keys can be provided in **two ways**:
    1. **Manually** set them in the [application.conf](src/main/resources/application.conf) file.
    2. **Set them as environment variables**:
       ```sh
       export MARVEL_API_PRIVATE_KEY=your_private_key
       export MARVEL_API_PUBLIC_KEY=your_public_key
       ```

### **Running with SBT**

If SBT is installed, simply execute from the project root directory:

```sh
sbt run
```

### **Running with Docker**

If SBT is not installed, you can run the project using Docker. In this case, ensure you pass the required API keys:

#### **Building the Docker Image**

```sh
docker build --build-arg MARVEL_API_PRIVATE_KEY=your_private_key \
             --build-arg MARVEL_API_PUBLIC_KEY=your_public_key \
             -t finagle-assignment .
```

#### **Running the Container**

```sh
docker run --rm -p 8080:8080 \
  -e MARVEL_API_PRIVATE_KEY=your_private_key \
  -e MARVEL_API_PUBLIC_KEY=your_public_key \
  finagle-assignment
```

---

## Using the API

### **Swagger UI**

- The easiest way to interact with the API is through **Swagger UI** at:
  [http://localhost:8080/v1/swagger-ui](http://localhost:8080/v1/swagger-ui).

### **cURL Example**

You can also query the API using cURL (or another REST client):

```sh
curl "http://localhost:8080/v1/marvel-characters?name=kingpin"
```

---

