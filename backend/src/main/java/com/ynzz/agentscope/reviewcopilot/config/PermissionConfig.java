package com.ynzz.agentscope.reviewcopilot.config;

import com.ynzz.agentscope.reviewcopilot.permission.ReviewPathGuard;
import com.ynzz.agentscope.reviewcopilot.permission.ReviewPermissionPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PermissionConfig {

    @Bean
    public ReviewPathGuard reviewPathGuard(ReviewCopilotProperties properties) {
        return new ReviewPathGuard(properties);
    }

    @Bean
    public ReviewPermissionPolicy reviewPermissionPolicy(ReviewPathGuard pathGuard) {
        return new ReviewPermissionPolicy(pathGuard);
    }
}
