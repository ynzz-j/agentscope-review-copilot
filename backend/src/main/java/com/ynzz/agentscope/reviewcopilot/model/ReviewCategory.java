package com.ynzz.agentscope.reviewcopilot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;

public enum ReviewCategory {
    BUG_RISK("bug-risk"),
    MAINTAINABILITY("maintainability"),
    CONCURRENCY("concurrency"),
    API_CONTRACT("api-contract"),
    TEST_GAP("test-gap"),
    AGENT_BOUNDARY("agent-boundary");

    private final String code;

    ReviewCategory(String code) {
        this.code = code;
    }

    @JsonValue
    public String getCode() {
        return code;
    }

    @JsonCreator
    public static ReviewCategory from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Review category must not be blank");
        }
        String normalized = value.trim().toLowerCase();
        return Arrays.stream(values())
                .filter(category -> category.code.equals(normalized)
                        || category.name().equalsIgnoreCase(normalized.replace('-', '_')))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown review category: " + value));
    }
}
