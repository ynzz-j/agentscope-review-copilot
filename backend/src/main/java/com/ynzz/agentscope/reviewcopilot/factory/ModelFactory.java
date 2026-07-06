package com.ynzz.agentscope.reviewcopilot.factory;

import io.agentscope.core.model.Model;

public interface ModelFactory {

    String MISSING_PROVIDER_MESSAGE =
            "Model provider is not configured. Choose one supported AgentScope-Java RC4 provider in application.yml or environment variables.";

    Model getModel();

    boolean isConfigured();
}
