package com.ynzz.agentscope.reviewcopilot.config;

import com.ynzz.agentscope.reviewcopilot.factory.ConfigurableModelFactory;
import com.ynzz.agentscope.reviewcopilot.factory.ModelFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ModelConfig {

    @Bean
    public ModelFactory modelFactory(ReviewCopilotProperties properties) {
        return new ConfigurableModelFactory(properties);
    }
}
