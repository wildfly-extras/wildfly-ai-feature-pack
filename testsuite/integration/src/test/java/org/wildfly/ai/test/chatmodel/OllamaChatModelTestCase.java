package org.wildfly.ai.test.chatmodel;

import dev.langchain4j.model.chat.ChatModel;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit5.ArquillianExtension;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.wildfly.ai.test.container.OllamaContainerManager;
import org.wildfly.ai.test.util.DeploymentFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for Ollama chat model CDI injection and basic functionality.
 * Tests the ai-chat-ollama Galleon layer.
 */
@ExtendWith(ArquillianExtension.class)
public class OllamaChatModelTestCase {

    @Deployment
    public static WebArchive createDeployment() {
        return DeploymentFactory.createBaseDeployment("ollama-chat-test.war");
    }

    @Inject
    @Named("ollama")
    private ChatModel chatModel;

    @BeforeAll
    public static void setupContainer() throws Exception {
        OllamaContainerManager.initializeContainer();
    }

    @Test
    public void testChatModelInjection() {
        assertThat(chatModel)
                .as("ChatModel should be injected via CDI")
                .isNotNull();
    }

    @Test
    public void testBasicChatInteraction() {
        String response = chatModel.chat("Say 'Hello, WildFly AI!' and nothing else.");

        assertThat(response)
                .as("Chat model should generate a response")
                .isNotNull()
                .isNotEmpty()
                .containsIgnoringCase("Hello");
    }

    @Test
    public void testMathQuestion() {
        String response = chatModel.chat("What is 2+2? Answer with just the number.");

        assertThat(response)
                .as("Chat model should correctly answer simple math")
                .isNotNull()
                .contains("4");
    }
}
