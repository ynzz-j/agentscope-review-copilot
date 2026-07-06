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
                "模型提供商 '"
                        + provider
                        + "' 已配置，但本示例不会绑定默认 provider adapter。启用模型评审前，请先加入匹配的 AgentScope-Java RC4 模型扩展。");
    }

    @Override
    public boolean isConfigured() {
        return StringUtils.hasText(properties.getModel().getProvider());
    }
}
