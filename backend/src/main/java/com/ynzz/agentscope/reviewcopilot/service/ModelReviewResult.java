package com.ynzz.agentscope.reviewcopilot.service;

import com.ynzz.agentscope.reviewcopilot.model.ReviewFinding;
import java.util.List;

public record ModelReviewResult(String summary, List<ReviewFinding> findings, String rawResponse) {

    public ModelReviewResult {
        findings = findings == null ? List.of() : List.copyOf(findings);
    }
}
