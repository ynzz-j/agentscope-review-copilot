package com.ynzz.agentscope.reviewcopilot.factory;

import com.ynzz.agentscope.reviewcopilot.config.ReviewCopilotProperties;
import io.agentscope.core.model.Model;
import org.springframework.util.StringUtils;

public class ConfigurableModelFactory implements ModelFactory {

    private final ReviewCopilotProperties properties;

    public ConfigurableModelFactory(ReviewCopilotProperties properties) {
        this.properties = properties;
    }

    @Override
    public Model getModel() {
        String provider = properties.getModel().getProvider();
        if (!StringUtils.hasText(provider)) {
            throw new ModelConfigurationException(MISSING_PROVIDER_MESSAGE);
        }
        throw new ModelConfigurationException(
                "Model provider '"
                        + provider
                        + "' is configured, but this sample does not bind a default provider adapter. Add the matching AgentScope-Java RC4 model extension before enabling model review.");
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(properties.getModel().getProvider());
    }
}
