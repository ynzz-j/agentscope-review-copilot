package com.ynzz.agentscope.reviewcopilot.config;

import com.ynzz.agentscope.reviewcopilot.store.JsonFileReviewJobStore;
import com.ynzz.agentscope.reviewcopilot.store.ReviewJobStore;
import io.agentscope.core.state.AgentStateStore;
import io.agentscope.core.state.JsonFileAgentStateStore;
import java.io.IOException;
import java.nio.file.Files;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StateStoreConfig {

    @Bean
    public AgentStateStore agentStateStore(ReviewCopilotProperties properties) {
        return new JsonFileAgentStateStore(properties.getStorage().getAgentStateDir());
    }

    @Bean
    public ReviewJobStore reviewJobStore(ReviewCopilotProperties properties) throws IOException {
        Files.createDirectories(properties.getStorage().getJobDir());
        return new JsonFileReviewJobStore(properties.getStorage().getJobDir());
    }
}
