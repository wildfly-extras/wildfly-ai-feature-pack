package org.wildfly.ai.test.observable;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test implementation of ChatModelListener for verifying that custom listeners
 * are preserved when observable is set to false.
 *
 * <p>This listener tracks counts of requests, responses, and errors for test validation.</p>
 */
@ApplicationScoped
public class TestChatModelListener implements ChatModelListener {

    private final AtomicInteger requestCount = new AtomicInteger(0);
    private final AtomicInteger responseCount = new AtomicInteger(0);
    private final AtomicInteger errorCount = new AtomicInteger(0);

    @Override
    public void onRequest(ChatModelRequestContext requestContext) {
        requestCount.incrementAndGet();
    }

    @Override
    public void onResponse(ChatModelResponseContext responseContext) {
        responseCount.incrementAndGet();
    }

    @Override
    public void onError(ChatModelErrorContext errorContext) {
        errorCount.incrementAndGet();
    }

    public int getRequestCount() {
        return requestCount.get();
    }

    public int getResponseCount() {
        return responseCount.get();
    }

    public int getErrorCount() {
        return errorCount.get();
    }

    public void reset() {
        requestCount.set(0);
        responseCount.set(0);
        errorCount.set(0);
    }
}
