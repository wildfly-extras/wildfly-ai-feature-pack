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

For each AI type it supports, the feature-pack provides 5 Galleon layers that build upon each other :
 * Support for Ollama: `ollama` layer
 * Support for embedded embeddings model: `embeddings`layer
 * Support for Open AI client API (to connect to AzureAI, ChatGPT or Groq for example):  `openai`layer
 * Support for Weaviate vectord database as an embedding store: `weaviate`layer
 * Support for web search engines as content providers: `web-search-engines` layer

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
  <feature-pack location="org.wildfly:wildfly-galleon-pack:33.0.0.Final">
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
      <include name="ollama"/>
      <include name="embeddings"/>
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
    <location>org.wildfly:wildfly-galleon-pack:33.0.0.Final</location>
  </feature-pack>
  <feature-pack>
    <location>org.wildfly:wildfly-ai-galleon-pack:1.0.0-SNAPSHOT</location>
  </feature-pack>
</feature-packs>
<layers>
  <!-- Base layer -->
  <layer>cloud-server</layer>
  <layer>ollama</layer>
  <layer>embeddings</layer>
</layers>
...
```

This [example](https://github.com/ehsavoie/webchat/) contains a complete WildFly Maven Plugin configuration.