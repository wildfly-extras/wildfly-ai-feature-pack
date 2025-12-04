WildFly AI Feature Pack
========================

This feature-pack for WildFly simplifies the integration of AI in applications.
The AI Galleon feature-pack is to be provisioned along with the WildFly Galleon feature-pack.

The Galleon layers defined in these feature-packs are decorator layers. This means that they need to be provisioned 
in addition to a WildFly base layer. The WildFly [Installation Guide](https://docs.wildfly.org/33/#installation-guides) covers the 
[base layers](https://docs.wildfly.org/33/Galleon_Guide.html#wildfly_foundational_galleon_layers) that WildFly defines.

NOTE: The base layer `ai` (that provisions WildFly AI subsystem) is the minimal base layer to use when provisioning Galleon layers that these 
feature-packs define.

Resources:

* [WildFly Installation Guide](https://docs.wildfly.org/33/#installation-guides)
* [Galleon documentation](https://docs.wildfly.org/galleon/)

Galleon feature-pack compatible with WildFly
========================

The Maven coordinates to use is: `org.wildfly:wildfly-ai-galleon-pack:<version>`

Supported AI types
========================

For each AI type it supports, the feature-pack provides several Galleon layers that build upon each other :
* Support for chat models to interact with a LLM:
  * `gemini-chat-model` 
  * `github-chat-model` 
  * `groq-chat-model` (same as openai-chat-model but targeting Groq)
  * `mistral-ai-chat-model`
  * `ollama-chat-model`
  * `openai-chat-model` 
* Support for streaming chat models to interact with a LLM:
  * `github-streaming-chat-model` 
  * `groq-streaming-chat-model` (same as openai-chat-model but targeting Groq)
  * `mistral-ai-streaming-chat-model`
  * `ollama-streaming-chat-model`
  * `openai-streaming-chat-model` 
* Support for embedding models: 
  * `in-memory-embedding-model-all-minilm-l6-v2`
  * `in-memory-embedding-model-all-minilm-l6-v2-q`
  * `in-memory-embedding-model-bge-small-en`
  * `in-memory-embedding-model-bge-small-en-q`
  * `in-memory-embedding-model-bge-small-en-v15`
  * `in-memory-embedding-model-bge-small-en-v15-q`
  * `in-memory-embedding-model-e5-small-v2`
  * `in-memory-embedding-model-e5-small-v2-q`
  * `ollama-embedding-model`
* Support for embedding stores:
  * `in-memory-embedding-store`
  * `neo4j-embedding-store`
  * `weaviate-embedding-store`
  * `chroma-embedding-store`
* Support for content retriever for RAG:
  * `default-embedding-content-retriever`: default content retriever using an `in-memory-embedding-store` and `in-memory-embedding-model-all-minilm-l6-v2` for embedding model.
  * `neo4j-content-retriever`
  * `web-search-engines`
* Support for [Model Context Protocol (MCP)](https://modelcontextprotocol.io/): 
  * `mcp-stream`: MCP Client using the Streamable (stream) transport
  * `mcp-sse`: MCP Client using the Server-Sent Events (SSE) transport
  * `mcp-stdio`: MCP Client using the Standard Input/Output (stdio) transport
  
For more details on these you can take a look at [LangChain4J](https://docs.langchain4j.dev/) and [Smallrye-llm](https://github.com/smallrye/smallrye-llm).

Using the WildFly AI Feature Pack
==========================

Provisioning of AI tools Galleon layers can be done in multiple ways according to the provisioning tooling in use.

## Provisioning using CLI tool

You can download the latest Galleon CLI tool from the Galleon github project [releases](https://github.com/wildfly/galleon/releases).
 
You need to define a Galleon provisioning configuration file such as:

```xml
<?xml version="1.0" ?>
<installation xmlns="urn:jboss:galleon:provisioning:3.0">
  <feature-pack location="org.wildfly:wildfly-galleon-pack:34.0.0.Final">
    <default-configs inherit="false"/>
    <packages inherit="false"/>
  </feature-pack>
  <feature-pack location="org.wildfly:wildfly-ai-galleon-pack:1.0.0-SNAPSHOT">
    <default-configs inherit="false"/>
    <packages inherit="false"/>
  </feature-pack>
  <config model="standalone" name="standalone.xml">
    <layers>
      <!-- Base layer -->
      <include name="cloud-server"/>
      <include name="ollama-chat-model"/>
      <include name="default-embedding-content-retriever"/>
    </layers>
  </config>
  <options>
    <option name="optional-packages" value="passive+"/>
    <option name="jboss-fork-embedded" value="true"/>
  </options>
</installation>
```
and provision it using the following command:

```
galleon.sh provision provisioning.xml --dir=my-wildfly-server
```

## Provisioning using the [WildFly Maven Plugin](https://github.com/wildfly/wildfly-maven-plugin/) or the [WildFly JAR Maven plugin](https://github.com/wildfly-extras/wildfly-jar-maven-plugin/)

You need to include the AI feature-pack and layers in the Maven Plugin configuration. This looks like:

```xml
...
<feature-packs>
  <feature-pack>
    <location>org.wildfly:wildfly-galleon-pack:36.0.0.Final</location>
  </feature-pack>
  <feature-pack>
    <location>org.wildfly:wildfly-ai-galleon-pack:1.0.0-SNAPSHOT</location>
  </feature-pack>
</feature-packs>
<layers>
    <!-- layers may be used to customize the server to provision-->
    <layer>cloud-server</layer>
    <layer>ollama-chat-model</layer>
    <layer>default-embedding-content-retriever</layer>
    <!-- providing the following layers -->
    <!--
      <layer>in-memory-embedding-model-all-minilm-l6-v2</layer>
      <layer>in-memory-embedding-store</layer>
    -->
    <!-- Exisiting layers that can be used -->
    <!--
      <layer>ollama-embedding-model</layer>
      <layer>openai-chat-model</layer>
      <layer>mistral-ai-chat-model</layer>
      <layer>neo4j-embedding-store</layer>
      <layer>weaviate-embedding-store</layer>
      <layer>web-search-engines</layer>
    -->
</layers>
...
```

## Provisioning using the [WildFly Maven Plugin](https://github.com/wildfly/wildfly-maven-plugin/) with Glow

```xml
...
  <groupId>org.wildfly.plugins</groupId>
  <artifactId>wildfly-maven-plugin</artifactId>
  <version>${version.wildfly.maven.plugin}</version>
    <configuration>
      <discoverProvisioningInfo>
        <spaces>
          <space>incubating</space>
        </spaces>
          <version>${version.wildfly.server}</version>
        </discoverProvisioningInfo>
        <name>ROOT.war</name>
        ...
    </configuration>
...
```

This [example](https://github.com/ehsavoie/webchat/) contains a complete WildFly Maven Plugin configuration.


[EXPERIMENTAL] Model Context Protocol Server
==========================

The feature pack supports also in a very experimental way the expose of your JakartaEE application as a [Model Context Protocol Server](https://spec.modelcontextprotocol.io/specification/2024-11-05/).
What you need to do in that case is to use the `org.wildfly:wildfly-mcp-api` artifact as a provided dependency and annotate the code you want to expose with the annotations provided by the API.

You may want to take a look at [wildfly-weather](https://github.com/ehsavoie/wildfly-weather) example.

You can then use [widldfly-mcp-chatbot](https://github.com/wildfly-extras/wildfly-mcp/tree/main/wildfly-chat-bot) from the [wildfly-mcp](https://github.com/wildfly-extras/wildfly-mcp) project to connect via Server-Sent-Event to it and play with your tools.
### Securing the MCP Server

To secure your MCP Server, bearer token authentication via OIDC is handled by the `elytron-oidc-client` subsystem. You can configure this mechanism using Keycloak. You can use the Keycloak container image:

```bash
podman volume create keycloack
podman run -p 8080:8080 -e KC_BOOTSTRAP_ADMIN_USERNAME=admin -e KC_BOOTSTRAP_ADMIN_PASSWORD=admin -v keycloack:/opt/keycloak/data/ quay.io/keycloak/keycloak:26.2.1 start-dev
```

Then you need to set-up Keycloack creating a realm *myrealm*, following the instructions provided [there](https://www.wildfly.org/guides/security-oidc-management-console) and create a user.
In your application you need to add the following section in your web.xml:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="https://jakarta.ee/xml/ns/jakartaee"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/web-app_6_0.xsd"
   version="6.0">
   ...
    <login-config>
        <auth-method>OIDC</auth-method>
    </login-config>
    ...
</web-app>
```
Then you need to secure your application using the *elytron-oidc-client* subsystem with a cli script like this one:

```bash
/subsystem=elytron-oidc-client/secure-deployment=ROOT.war:add(client-id=mcp-client, bearer-only=true, provider-url="${env.OIDC_PROVIDER_URL:http://localhost:8080}/realms/myrealm", ssl-required=EXTERNAL, public-client="true", principal-attribute="preferred_username")
```
Please note that the secured deployment MUST be configured with `bearer-only=true` within the `elytron-oidc-client` subsystem, as this ensures the MCP server relies on the bearer token provided by the MCP client for authentication.

To get the token associated to a user you can use the following command:

```
curl -X POST http://localhost:8080/realms/myrealm/protocol/openid-connect/token -H 'content-type: application/x-www-form-urlencoded' -d 'client_id=mcp-client&client_secret=UmqLUYjlRbDXZqa6vsiOmonjysIxTL7W' -d 'username=myuser&password=myuser&grant_type=password' | jq --raw-output '.access_token'
```

[EXPERIMENTAL] WASM Support
==========================


The feature pack supports also in a very experimental way [Wasm Wasi](https://wasi.dev/) modules.
What you need to do in that case is to use the `org.wildfly:wildfly-wasm-api` artifact as a provided dependency and annotate the code you want to expose with the annotations provided by the API.
Wasm binaries can be defined in the `wasm subsystem` to be injected as `org.wildfly.wasm.api.WasmInvoker` via CDI. You can even expose `org.wildfly.wasm.api.WasmToolService` as MCP tools.

You may want to take a look at [wildfly-weather](https://github.com/ehsavoie/wildfly-weather/compare/wasm_subsystem) example.


Releasing
==========================

```
mvn release:prepare 
mvn release:perform -Pjboss-release
git push origin main
git push origin --tags
nxrm3:staging-move
```
