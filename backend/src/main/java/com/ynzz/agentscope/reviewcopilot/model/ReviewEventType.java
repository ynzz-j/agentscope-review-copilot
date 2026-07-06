package com.ynzz.agentscope.reviewcopilot.model;

public enum ReviewEventType {
    JOB_CREATED,
    DIFF_LOADED,
    FILE_CONTEXT_LOADED,
    RULE_CHECK_DONE,
    MODEL_REVIEWING,
    FINDING_GENERATED,
    REPORT_READY,
    JOB_COMPLETED,
    JOB_FAILED
}
