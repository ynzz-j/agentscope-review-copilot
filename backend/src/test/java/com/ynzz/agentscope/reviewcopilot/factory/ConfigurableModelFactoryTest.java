package com.ynzz.agentscope.reviewcopilot.factory;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

import com.ynzz.agentscope.reviewcopilot.config.ReviewCopilotProperties;
import io.agentscope.core.model.Model;
import org.junit.jupiter.api.Test;

class ConfigurableModelFactoryTest {

    @Test
    void rejectsMissingProviderWithoutDefaulting() {
        ConfigurableModelFactory factory = new ConfigurableModelFactory(new ReviewCopilotProperties());

        assertThatThrownBy(factory::getModel)
                .isInstanceOf(ModelConfigurationException.class)
                .hasMessageContaining("未配置模型提供商");
    }

    @Test
    void createsDashScopeModelForDspAlias() {
        ReviewCopilotProperties properties = new ReviewCopilotProperties();
        properties.getModel().setProvider("dsp");
        properties.getModel().setModelName("qwen-plus");
        properties.getModel().setApiKey("test-key");
        ConfigurableModelFactory factory = new ConfigurableModelFactory(properties);

        Model model = factory.getModel();

        assertThat(model.getModelName()).isEqualTo("qwen-plus");
    }

    @Test
    void rejectsConfiguredProviderWithoutModelName() {
        ReviewCopilotProperties properties = new ReviewCopilotProperties();
        properties.getModel().setProvider("dsp");
        properties.getModel().setApiKey("test-key");
        ConfigurableModelFactory factory = new ConfigurableModelFactory(properties);

        assertThatThrownBy(factory::getModel)
                .isInstanceOf(ModelConfigurationException.class)
                .hasMessageContaining("缺少 model-name");
    }
}
