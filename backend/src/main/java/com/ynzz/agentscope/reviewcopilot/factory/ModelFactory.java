package com.ynzz.agentscope.reviewcopilot.factory;

import io.agentscope.core.model.Model;

public interface ModelFactory {

    String MISSING_PROVIDER_MESSAGE =
            "未配置模型提供商。请在 application.yml 或环境变量中显式选择一个 AgentScope-Java RC4 支持的 provider。";

    Model getModel();

    boolean isConfigured();
}
