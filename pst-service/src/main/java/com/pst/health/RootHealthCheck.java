package com.pst.health;

import com.codahale.metrics.health.HealthCheck;

public class RootHealthCheck extends HealthCheck {
    private final String message;

    public RootHealthCheck(String message) {
        this.message = message;
    }

    @Override
    protected Result check() throws Exception {
        final String content = String.format(message, "TEST");
        if (!content.contains("TEST")) {
            return Result.unhealthy("message doesn't include a name");
        }
        return Result.healthy();
    }
}