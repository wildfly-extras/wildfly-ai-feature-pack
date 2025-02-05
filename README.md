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

The Maven coordinates to use is: `org.wildfly:wildfly-ai-galleon-pack::<version>`

Supported AI types
========================

For each AI type it supports, the feature-pack provides 17 Galleon layers that build upon each other :
* Support for chat models to interact with a LLM:
  * `mistral-ai-chat-model`
  * `ollama-chat-model`
  * `groq-chat-model` (same as openai-chat-model but targeting Groq)
  * `openai-chat-model` 
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
* Support for content retriever for RAG:
  * `default-embedding-content-retriever`: default content retriever using an `in-memory-embedding-store` and `in-memory-embedding-model-all-minilm-l6-v2` for embedding model.
  * `web-search-engines`

For more details on these you can take a look at [LangChain4J](https://docs.langchain4j.dev/) and [Smallrye-llm](https://github.com/smallrye/smallrye-llm).

Using the WildFly AI Feature Pack
==========================

Provisioning of AI tools Galleon layers can be done in multiple ways according to the provisioning tooling in use.

## Provisioning using CLI tool

You can download the latest Galleon CLI tool from the Galleon github project [releases](https://github.com/wildfly/galleon/releases).
 
You need to define a Galleon provisioning configuration file such as:

```
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

You need to include the datasources feature-pack and layers in the Maven Plugin configuration. This looks like:

```
...
<feature-packs>
  <feature-pack>
    <location>org.wildfly:wildfly-galleon-pack:34.0.0.Final</location>
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
    <!-- Exisiting layers thart can be used -->
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

This [example](https://github.com/ehsavoie/webchat/) contains a complete WildFly Maven Plugin configuration.
