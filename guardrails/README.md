# Guardrails Example

[LangChain4j Guardrails](https://docs.langchain4j.dev/tutorials/guardrails) let you validate or
rewrite the messages going into and coming out of an AI service. Support for them comes from
`langchain4j-cdi` (already a transitive dependency of every `ollama-chat-model` /
`openai-chat-model` / ... layer) via extra attributes on `@RegisterAIService` - there is no
separate Galleon layer to provision.

## What it shows

* `ProfanityInputGuardrail` - an `InputGuardrail` that rejects the request *before it reaches the
  model* if the message contains a disallowed word.
* `ResponseLengthOutputGuardrail` - an `OutputGuardrail` that *rewrites* the model's answer
  (truncating it) instead of rejecting it.
* `Assistant` - wires both in with `@RegisterAIService(chatModelName = "ollama", inputGuardrails =
  ProfanityInputGuardrail.class, outputGuardrails = ResponseLengthOutputGuardrail.class)`.
* `GuardrailExceptionMapper` - a JAX-RS `ExceptionMapper` turning the `GuardrailException` thrown
  by a failed input guardrail into an HTTP 422 response instead of a generic 500.

## Prerequisites

* JDK 17+
* Maven 3.9+
* [Ollama](https://ollama.com/) running locally with `llama3.1:8b` pulled:

```shell
ollama pull llama3.1:8b
```

## Build and run

Start the application using:

```shell
mvn wildfly:dev
```

This provisions a WildFly server with the `ollama-chat-model` layer at the experimental
stability level and deploys the application. The server remains running in the foreground.

## Trying it out

The REST endpoint is available at:

```text
http://localhost:8080/guardrails-assistant/api/chat
```

### Normal request

A normal request passes through to the model:

```shell
curl -s -X POST http://localhost:8080/guardrails-assistant/api/chat \
    -H 'Content-Type: text/plain' \
    -d 'Say hello in one short sentence.'
```

Example response:

```text
Hello!
```

### Blocked input

The input guardrail rejects the request before Ollama is called:

```shell
curl -i -X POST http://localhost:8080/guardrails-assistant/api/chat \
    -H 'Content-Type: text/plain' \
    -d 'You are stupid.'
```

Example response:

```text
HTTP/1.1 422 Unprocessable Entity
Content-Type: text/plain;charset=UTF-8

The guardrail org.wildfly.ai.examples.guardrails.ProfanityInputGuardrail failed with this message:
The message contains disallowed language ('stupid')
```

### Long response

The output guardrail rewrites the model response instead of rejecting it:

```shell
curl -s -X POST http://localhost:8080/guardrails-assistant/api/chat \
    -H 'Content-Type: text/plain' \
    -d 'Write a detailed 300-word essay about WildFly application server.'
```

Example response:

```text
WildFly is an open-source Java application server ... (truncated by output guardrail)
```

## Implementation details

The AI service registers the guardrails using `@RegisterAIService`:

```java
@RegisterAIService(
        chatModelName = "ollama",
        inputGuardrails = ProfanityInputGuardrail.class,
        outputGuardrails = ResponseLengthOutputGuardrail.class
)
public interface Assistant {
}
```

The input guardrail runs before the request reaches the AI model.

The output guardrail runs after the model generates a response and can either:

* accept the response unchanged,
* reject it,
* or rewrite it before returning it to the caller.

## Notes

This example uses simple guardrail implementations for demonstration purposes:

* The input guardrail uses a small deny-list of words.
* The output guardrail truncates responses longer than the configured maximum length.

In a real application, guardrails can integrate with external moderation services,
business rules, validation systems, or other application-specific logic.