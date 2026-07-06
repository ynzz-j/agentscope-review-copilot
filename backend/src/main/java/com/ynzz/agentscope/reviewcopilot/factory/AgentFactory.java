package com.ynzz.agentscope.reviewcopilot.factory;

import com.ynzz.agentscope.reviewcopilot.config.ReviewCopilotProperties;
import io.agentscope.core.ReActAgent;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.tool.Toolkit;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class AgentFactory {

    private final ModelFactory modelFactory;
    private final Toolkit toolkit;
    private final List<MiddlewareBase> middlewares;
    private final ReviewCopilotProperties properties;

    public AgentFactory(
            ModelFactory modelFactory,
            Toolkit toolkit,
            List<MiddlewareBase> middlewares,
            ReviewCopilotProperties properties) {
        this.modelFactory = modelFactory;
        this.toolkit = toolkit;
        this.middlewares = middlewares;
        this.properties = properties;
    }

    public boolean isModelConfigured() {
        return modelFactory.isConfigured();
    }

    public ReActAgent create(String sessionId) {
        return ReActAgent.builder()
                .name(properties.getAgent().getName())
                .sysPrompt(properties.getAgent().getSysPrompt())
                .model(modelFactory.getModel())
                .toolkit(toolkit)
                .middlewares(middlewares)
                .maxIters(properties.getAgent().getMaxIters())
                .build();
    }
}
