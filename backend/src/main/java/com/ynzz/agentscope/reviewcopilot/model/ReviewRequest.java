package com.ynzz.agentscope.reviewcopilot.model;

import jakarta.validation.constraints.NotBlank;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public record ReviewRequest(
        @NotBlank String repoPath,
        DiffMode diffMode,
        String baseRef,
        String sessionId,
        List<ReviewCategory> focusCategories) {

    public DiffMode normalizedDiffMode() {
        return diffMode == null ? DiffMode.WORKING_TREE : diffMode;
    }

    public String normalizedSessionId() {
        return sessionId == null || sessionId.isBlank()
                ? "session-" + UUID.randomUUID()
                : sessionId.trim();
    }

    public List<ReviewCategory> normalizedFocusCategories() {
        if (focusCategories == null || focusCategories.isEmpty()) {
            return Arrays.asList(ReviewCategory.values());
        }
        return List.copyOf(focusCategories);
    }
}
