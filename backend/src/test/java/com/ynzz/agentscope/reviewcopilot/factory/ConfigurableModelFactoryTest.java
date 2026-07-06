package com.ynzz.agentscope.reviewcopilot.factory;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ynzz.agentscope.reviewcopilot.config.ReviewCopilotProperties;
import org.junit.jupiter.api.Test;

class ConfigurableModelFactoryTest {

    @Test
    void rejectsMissingProviderWithoutDefaulting() {
        ConfigurableModelFactory factory = new ConfigurableModelFactory(new ReviewCopilotProperties());

        assertThatThrownBy(factory::getModel)
                .isInstanceOf(ModelConfigurationException.class)
                .hasMessageContaining("Model provider is not configured");
    }
}
