package dev.langchain4j.cdi.telemetry;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;

/**
 * Mock MetricsChatModelListener for testing purposes only.
 * Simulates the actual langchain4j-cdi MetricsChatModelListener class.
 */
public class MetricsChatModelListener implements ChatModelListener {
    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
    }
}
