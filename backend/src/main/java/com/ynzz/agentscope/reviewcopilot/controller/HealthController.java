package com.ynzz.agentscope.reviewcopilot.controller;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "application", "agentscope-review-copilot",
                "version", "2.0.0-RC4");
    }
}
