package com.ynzz.agentscope.reviewcopilot;

import com.ynzz.agentscope.reviewcopilot.config.ReviewCopilotProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ReviewCopilotProperties.class)
public class ReviewCopilotApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReviewCopilotApplication.class, args);
    }
}
