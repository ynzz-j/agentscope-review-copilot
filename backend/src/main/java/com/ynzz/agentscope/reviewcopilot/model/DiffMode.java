package com.ynzz.agentscope.reviewcopilot.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum DiffMode {
    WORKING_TREE,
    STAGED,
    BASE_REF;

    @JsonCreator
    public static DiffMode from(String value) {
        if (value == null || value.isBlank()) {
            return WORKING_TREE;
        }
        return DiffMode.valueOf(value.trim().replace('-', '_').toUpperCase());
    }
}
