# Changelog

## 0.10.0 (Unreleased)

### Breaking Changes

#### MCP Server Annotation API

The MCP server annotation API has been replaced by the **[`org.mcp-java:mcp-annotations`](https://github.com/mcp-java/java-mcp-annotations)** library. This change standardizes annotations and APIs across Java runtimes, decoupling the MCP annotation contract from the WildFly-specific `wildfly-mcp/api` module.

**Migration steps:**

1. Replace the `wildfly-mcp/api` dependency with `org.mcp-java:mcp-annotations`:

   ```xml
   <dependency>
       <groupId>org.mcp-java</groupId>
       <artifactId>mcp-annotations</artifactId>
       <scope>provided</scope>
   </dependency>
   ```

2. Update your imports to use the new package names from `org.mcp-java:mcp-annotations`.

See the [java-mcp-annotations](https://github.com/mcp-java/java-mcp-annotations) project for the full API reference.

### New Features

* Standardized MCP annotations via [`org.mcp-java:mcp-annotations`](https://github.com/mcp-java/java-mcp-annotations) for cross-runtime compatibility.
* Async execution support: all chat model layers now support the `executor-service` attribute for configuring a `ManagedExecutorService`.
* WildFly Preview compatibility.

---

## 0.9.1

Initial public release.
