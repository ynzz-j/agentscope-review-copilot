package com.ynzz.agentscope.reviewcopilot.permission;

import java.util.regex.Pattern;

public final class ReviewIdValidator {

    private static final Pattern SAFE_ID = Pattern.compile("[A-Za-z0-9._-]+");

    private ReviewIdValidator() {}

    public static String requireSafe(String value, String label) {
        if (value == null || value.isBlank() || !SAFE_ID.matcher(value).matches()) {
            throw new IllegalArgumentException(label + " must contain only letters, digits, dot, underscore, or dash");
        }
        return value;
    }
}
