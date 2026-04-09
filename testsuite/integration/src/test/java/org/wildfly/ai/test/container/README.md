# Container Managers

This package contains singleton managers for shared test containers used across integration tests.

## Overview

Container managers provide:
- **Singleton instances** - One container shared across all tests
- **Reuse detection** - Automatically detect and use existing local instances
- **Lifecycle management** - Automatic startup, initialization, and configuration
- **System properties** - Expose endpoints as system properties for tests

## Available Managers

### OllamaContainerManager

Manages the Ollama container for LLM chat model testing.

**Container**: `ollama/ollama:latest`

**Features**:
- Detects existing Ollama on `localhost:11434`
- Automatically pulls `llama3.2:1b` model on first start
- Static initialization (ready before tests run)
- **Automatic cleanup** - Container stopped when build finishes

**System Properties**:
- `ollama.base.url` - API endpoint (e.g., `http://localhost:11434`)
- `ollama.model.name` - Model name (`llama3.2:1b`)

**Usage**:
```java
@BeforeAll
public static void setup() throws Exception {
    OllamaContainerManager.initializeContainer();
}

@Test
public void testWithOllama() {
    String endpoint = OllamaContainerManager.getEndpoint();
    String model = OllamaContainerManager.getModelName();
    // Use in tests...
}
```

### LgtmContainerManager

Manages the Grafana LGTM (Loki, Grafana, Tempo, Mimir) container for OpenTelemetry testing.

**Container**: `grafana/otel-lgtm:0.17.1`

**Features**:
- Detects existing Grafana on `localhost:3000`
- Provides complete observability stack
- Static initialization (ready before tests run)
- Extended startup timeout (3 minutes)
- **Graceful degradation** - Tests skip automatically if Docker is unavailable
- **Automatic cleanup** - Container stopped when build finishes

**Services Provided**:
- **Grafana** (port 3000) - Visualization and dashboards
- **Tempo** (port 4318) - Distributed tracing (OTLP HTTP)
- **Prometheus/Mimir** (port 9090) - Metrics storage

**System Properties**:
- `lgtm.grafana.url` - Grafana web UI (e.g., `http://localhost:3000`)
- `lgtm.otlp.endpoint` - OTLP endpoint (e.g., `http://localhost:4318`)
- `lgtm.prometheus.url` - Prometheus API (e.g., `http://localhost:9090`)

**Usage**:
```java
@BeforeAll
public static void setup() throws Exception {
    LgtmContainerManager.initializeContainer();
}

@Test
public void testWithLgtm() {
    String grafanaUrl = LgtmContainerManager.getGrafanaUrl();
    String otlpEndpoint = LgtmContainerManager.getOtlpEndpoint();
    String prometheusUrl = LgtmContainerManager.getPrometheusUrl();
    // Use in tests...
}
```

## Architecture

### Automatic Initialization via JUnit Platform

Container managers are automatically initialized **before any tests run** using JUnit's `TestExecutionListener` mechanism:

**Initializer Classes:**
- `OllamaContainerInitializer` - Initializes Ollama before tests
- `LgtmContainerInitializer` - Initializes LGTM stack before tests

**Registration:**
Both initializers are registered in:
```
src/test/resources/META-INF/services/org.junit.platform.launcher.TestExecutionListener
```

This ensures containers are ready when tests start, with no manual setup needed.

### Singleton Pattern
Each manager uses a singleton pattern with:
- Static initialization block
- Synchronized `initializeContainer()` method
- `volatile boolean initialized` flag
- Thread-safe, idempotent initialization

### Reuse Detection
Before starting a new container, managers check for existing local instances:

**OllamaContainerManager**:
```java
private static boolean isOllamaRunning(String endpoint) {
    // Check /api/tags endpoint with 2-second timeout
    return responseCode == 200;
}
```

**LgtmContainerManager**:
```java
private static boolean isGrafanaRunning(String grafanaUrl) {
    // Check root endpoint with 2-second timeout
    return responseCode == 200 || responseCode == 302; // 302 = login redirect
}
```

### Automatic Cleanup
Both managers register JVM shutdown hooks to ensure containers are stopped when the build finishes:

**OllamaContainerManager**:
- Registers shutdown hook after initialization
- Only stops containers started by Testcontainers (not existing local instances)
- Logs cleanup progress to console

**LgtmContainerManager**:
- Same shutdown hook behavior
- Ensures LGTM stack is properly stopped after tests complete

**Example Output**:
```
Stopping Ollama container...
Ollama container stopped successfully
Stopping LGTM container...
LGTM container stopped successfully
```

This ensures:
- Clean state for each test run
- No lingering containers consuming resources
- Proper cleanup in CI/CD environments

## Best Practices

### 1. No Manual Initialization Needed
Containers are **automatically initialized** by JUnit Platform before tests run:

```java
@ExtendWith(ArquillianExtension.class)
public class MyTest {
    // No @BeforeAll needed - containers already initialized!
    
    @Test
    public void testWithOllama() {
        String endpoint = OllamaContainerManager.getEndpoint();
        // Container is ready to use
    }
}
```

**Why?** The `OllamaContainerInitializer` and `LgtmContainerInitializer` classes are 
registered as JUnit `TestExecutionListener`s and run before any test execution.

### 2. Use Manager Methods, Not Direct Container Access
❌ **Don't**:
```java
@Container
static GenericContainer<?> lgtm = new GenericContainer<>(...);
lgtm.getMappedPort(3000); // Direct access
```

✅ **Do**:
```java
LgtmContainerManager.initializeContainer();
String url = LgtmContainerManager.getGrafanaUrl(); // Use manager
```

### 3. Handle Both Local and Containerized Scenarios
The managers automatically detect local instances, so tests work whether using:
- Local development setup (e.g., Ollama running on localhost)
- Testcontainers-managed instances
- Mix of both

### 4. Manual Initialization is Idempotent
If you need to manually call `initializeContainer()` for any reason, it's safe - 
the method is idempotent and will do nothing if already initialized:

```java
// Safe to call explicitly, but not required
OllamaContainerManager.initializeContainer();
```

## Troubleshooting

### Container Fails to Start

**Symptom**: `ContainerLaunch` exception during test execution

**Solutions**:
1. **Check Docker is running**:
   ```bash
   docker ps
   ```

2. **Increase startup timeout** (for slow machines):
   ```java
   // In LgtmContainerManager
   .withStartupTimeout(Duration.ofMinutes(5)) // Increase from 3
   ```

3. **Check for port conflicts**:
   ```bash
   # For Ollama
   netstat -an | grep 11434
   
   # For LGTM
   netstat -an | grep 3000
   netstat -an | grep 4318
   ```

4. **Pull image manually first**:
   ```bash
   docker pull grafana/otel-lgtm:0.17.1
   docker pull ollama/ollama:latest
   ```

### Tests Can't Connect to Container

**Symptom**: Connection refused or timeout errors in tests

**Solutions**:
1. **Wait for container to be fully ready**:
   - Ollama: Already waits for `/api/tags`
   - LGTM: Already waits for `/ready` endpoint

2. **Check mapped ports**:
   ```java
   System.out.println("Grafana URL: " + LgtmContainerManager.getGrafanaUrl());
   System.out.println("OTLP Endpoint: " + LgtmContainerManager.getOtlpEndpoint());
   ```

3. **Verify container health**:
   ```bash
   docker ps
   docker logs <container-id>
   ```

## Graceful Degradation

### LGTM Container (Optional)
The LGTM container manager gracefully handles environments where Docker is unavailable:

**When Docker is Available**:
- Starts LGTM container if Grafana not already running locally
- OpenTelemetry integration tests run normally

**When Docker is NOT Available**:
- LGTM initialization fails silently (no build failure)
- `LgtmContainerManager.isAvailable()` returns `false`
- OpenTelemetry integration tests are **skipped** automatically
- All other tests continue to run normally

**Test Output Example**:
```
=================================================
LGTM not available - OpenTelemetry tests disabled
To enable: Start Grafana locally on port 3000
  or ensure Docker is available
=================================================
...
Tests run: 33, Failures: 0, Errors: 0, Skipped: 4
```

This allows the test suite to run successfully in CI/CD environments or on developer machines where Docker may not be available.

## Performance Tips

### Use Local Instances for Development
For faster iteration during development, run services locally instead of starting containers for each test run:

```bash
# Start Ollama locally
ollama serve

# Start Grafana LGTM locally (in background)
docker run -d -p 3000:3000 -p 4318:4318 -p 9090:9090 \
  --name lgtm grafana/otel-lgtm:0.17.1
```

Managers will automatically detect and use these local instances instead of starting new containers. This provides:
- **Faster test execution** - No container startup time
- **Persistent state** - Models and data remain between test runs
- **Manual control** - Start/stop services as needed

To stop local instances:
```bash
# Stop Ollama (Ctrl+C if running in foreground)
# Or: pkill ollama

# Stop LGTM
docker stop lgtm
docker rm lgtm
```

## Integration with Arquillian

The container managers work seamlessly with Arquillian tests:

```java
@ExtendWith(ArquillianExtension.class)
public class MyIntegrationTest {
    
    @Deployment
    public static WebArchive createDeployment() {
        return DeploymentFactory.createBaseDeployment("my-test.war");
    }
    
    @BeforeAll
    public static void setupContainers() throws Exception {
        // Containers ready before Arquillian starts WildFly
        OllamaContainerManager.initializeContainer();
        LgtmContainerManager.initializeContainer();
    }
    
    @Inject
    @Named("ollama")
    private ChatModel chatModel;
    
    @Test
    public void testChatModel() {
        // Test using containerized Ollama
        String response = chatModel.chat("Hello");
        assertThat(response).isNotNull();
    }
}
```

## References

- [Testcontainers Documentation](https://www.testcontainers.org/)
- [Ollama Container](https://hub.docker.com/r/ollama/ollama)
- [Grafana LGTM Stack](https://github.com/grafana/docker-otel-lgtm)
- [Arquillian Testing](https://arquillian.org/)
