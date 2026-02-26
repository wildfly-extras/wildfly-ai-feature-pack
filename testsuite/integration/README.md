# WildFly AI Integration Tests

This module contains integration tests for the WildFly AI Feature Pack using Arquillian and Testcontainers.

## Overview

The integration tests automatically manage an Ollama container using Testcontainers, eliminating the need for manual setup. The container lifecycle is fully automated:

1. **Container Initialization**: When tests start, a JUnit Platform `TestExecutionListener` triggers the `OllamaContainerManager`
2. **Dynamic Configuration**: The container starts with a random port, and the endpoint is dynamically passed to WildFly
3. **Container Reuse**: The container is configured with `withReuse(true)`, so subsequent test runs reuse the same container
4. **Model Download**: On first run, the `llama3.2:1b` model is automatically pulled

## Running Tests

### From Maven

Run integration tests with the `integration-test` profile:

```bash
mvn clean verify -Pintegration-test
```

### From IDE

Tests can be run directly from your IDE. The `OllamaContainerInitializer` ensures the Ollama container starts before any tests execute.

## Architecture

### Key Components

1. **OllamaContainerManager** (`org.wildfly.ai.test.container.OllamaContainerManager`)
   - Singleton manager for Ollama container lifecycle
   - Detects existing Ollama instances on `localhost:11434` or starts a new container
   - Sets system properties for test access

2. **OllamaContainerInitializer** (`org.wildfly.ai.test.OllamaContainerInitializer`)
   - JUnit Platform `TestExecutionListener` that triggers container initialization
   - Registered via `META-INF/services/org.junit.platform.launcher.TestExecutionListener`
   - Ensures container starts before Arquillian launches WildFly

3. **Arquillian Configuration** (`src/test/resources/arquillian.xml`)
   - Configured to pass Ollama endpoint as system properties to WildFly
   - Properties: `org.wildfly.ai.ollama.chat.url`, `org.wildfly.ai.ollama.embedding.url`

### System Properties

The following system properties are set by `OllamaContainerManager`:

- `ollama.base.url`: Ollama API endpoint (e.g., `http://localhost:46239`)
- `ollama.model.name`: Model name (`llama3.2:1b`)

These properties are passed to WildFly via `arquillian.xml`:

- `org.wildfly.ai.ollama.chat.url`
- `org.wildfly.ai.ollama.embedding.url`
- `org.wildfly.ai.ollama.chat.model.name`
- `org.wildfly.ai.ollama.embedding.model.name`

## Test Categories

### Chat Model Tests
- **OllamaChatModelTestCase**: Tests basic chat model functionality
- **OllamaStreamingChatModelTestCase**: Tests streaming chat capabilities

### Embedding Model Tests
- **AllMiniLmL6V2EmbeddingModelTestCase**: Tests in-memory embedding model
- **OllamaEmbeddingModelTestCase**: Tests Ollama embedding model

### Storage and Retrieval Tests
- **InMemoryEmbeddingStoreTestCase**: Tests in-memory embedding store
- **EmbeddingStoreContentRetrieverTestCase**: Tests content retrieval functionality

## Using Local Ollama Instance

If you have Ollama running locally on the default port (11434), the tests will automatically detect and use it instead of starting a new container:

```bash
# Start local Ollama
podman run -d -p 11434:11434 ollama/ollama:0.17.0
podman exec <container-name> ollama pull llama3.2:1b

# Run tests - will use existing instance
mvn verify -Pintegration-test
```

## Troubleshooting

### Container Not Starting

If the container fails to start, check:
- Docker/Podman is running: `podman ps` or `docker ps`
- No port conflicts on 11434 or the assigned random port
- Testcontainers has access to the container runtime

### Tests Failing with Connection Errors

- Verify the Ollama container is running: `podman ps`
- Check logs: `podman logs <container-name>`
- Ensure the model is downloaded: `podman exec <container-name> ollama list`

### Container Cleanup

Testcontainers with `withReuse(true)` keeps containers running between test runs. To clean up:

```bash
# List testcontainers
podman ps -a | grep testcontainers

# Stop and remove
podman stop <container-name> && podman rm <container-name>
```

## Dependencies

- **JUnit 5**: Test framework
- **Arquillian**: Container testing framework
- **Testcontainers**: Container lifecycle management
- **WildFly Arquillian**: WildFly managed container adapter
- **AssertJ**: Fluent assertions

## Configuration Files

- `pom.xml`: Maven configuration with dependencies and plugins
- `src/test/resources/arquillian.xml`: Arquillian container configuration
- `src/test/resources/provisioning.xml`: WildFly server provisioning
- `extra-content/`: Additional server configuration files
