/*
 * Copyright The WildFly Authors
 * SPDX-License-Identifier: Apache-2.0
 */
package org.wildfly.extension.ai.injection.observability;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequest;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponse;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import dev.langchain4j.model.output.TokenUsage;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import org.wildfly.extension.ai.injection.AILogger;

/**
 * Creates metrics following the <a href="https://opentelemetry.io/docs/specs/semconv/gen-ai/gen-ai-spans//">Semantic
 * Conventions for GenAI Spans</a>.
 */
@Dependent
public class OpenTelemetryTracesChatModelListener implements ChatModelListener {

    public OpenTelemetryTracesChatModelListener() {
    }

    private static final String OTEL_SCOPE_KEY_NAME = "OTelScope";
    private static final String OTEL_SPAN_KEY_NAME = "OTelSpan";

    @Inject
    private Instance<Tracer> tracerInstance;

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        if (!tracerInstance.isResolvable()) {
            return;
        }
        Tracer tracer = tracerInstance.get();
        final ChatModelRequest request = requestContext.request();
        SpanBuilder spanBuilder = tracer.spanBuilder("chat " + request.model())
                .setSpanKind(SpanKind.SERVER)
                .setAttribute("gen_ai.operation.name", "chat");
        if (requestContext.attributes().get(OTEL_SPAN_KEY_NAME) != null) {
            spanBuilder.setParent(Context.current().with((Span) requestContext.attributes().get(OTEL_SPAN_KEY_NAME)));
        }
        if (request.maxTokens() != null) {
            spanBuilder.setAttribute("gen_ai.request.max_tokens", request.maxTokens());
        }

        if (request.temperature() != null) {
            spanBuilder.setAttribute("gen_ai.request.temperature", request.temperature());
        }

        if (request.topP() != null) {
            spanBuilder.setAttribute("gen_ai.request.top_p", request.topP());
        }
        if (request.messages() != null && !request.messages().isEmpty()) {
            spanBuilder.setAttribute("gen_ai.request.messages", request.messages().toString());
        }
        Span span = spanBuilder.startSpan();
        Scope scope = span.makeCurrent();
        requestContext.attributes().put(OTEL_SCOPE_KEY_NAME, scope);
        requestContext.attributes().put(OTEL_SPAN_KEY_NAME, span);
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        Span span = (Span) responseContext.attributes().get(OTEL_SPAN_KEY_NAME);
        if (span != null) {
            ChatModelResponse response = responseContext.response();
            span.setAttribute("gen_ai.response.id", response.id())
                    .setAttribute("gen_ai.response.model", response.model());
            if (response.finishReason() != null) {
                span.setAttribute("gen_ai.response.finish_reasons", response.finishReason().toString());
            }
            TokenUsage tokenUsage = response.tokenUsage();
            if (tokenUsage != null) {
                span.setAttribute("gen_ai.usage.output_tokens", tokenUsage.outputTokenCount())
                        .setAttribute("gen_ai.usage.input_tokens", tokenUsage.inputTokenCount());
            }
            if (response.aiMessage() != null) {
                span.setAttribute("gen_ai.response.message", response.aiMessage().toString());
            }
            span.end();
        }
        Scope scope = (Scope) responseContext.attributes().get(OTEL_SCOPE_KEY_NAME);
        AILogger.ROOT_LOGGER.debug("OpenTelemetryChatModelListener.onResponse with context " + span.getSpanContext() + " with scope " + scope);
        closeScope(scope);
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        Span span = (Span) errorContext.attributes().get(OTEL_SPAN_KEY_NAME);
        if (span != null) {
            span.recordException(errorContext.error());
            span.end();
        }
        closeScope((Scope) errorContext.attributes().get(OTEL_SCOPE_KEY_NAME));
    }

    private void closeScope(Scope scope) {
        if (scope != null) {
            AILogger.ROOT_LOGGER.debug("OpenTelemetryChatModelListener.closeScope with scope " + scope);
            scope.close();
        }
    }
}
